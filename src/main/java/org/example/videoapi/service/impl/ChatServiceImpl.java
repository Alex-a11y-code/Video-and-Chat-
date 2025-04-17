package org.example.videoapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.google.common.hash.BloomFilter;
import org.example.videoapi.config.RabbitMQConfig;
import org.example.videoapi.mapper.GroupMapper;
import org.example.videoapi.mapper.MessageMapper;
import org.example.videoapi.mapper.CroupMessageReadStatusMapper;
import org.example.videoapi.pojo.entity.ChatGroup;
import org.example.videoapi.pojo.entity.GroupMember;
import org.example.videoapi.pojo.entity.Message;
import org.example.videoapi.service.BlockService;
import org.example.videoapi.service.ChatService;
import org.example.videoapi.websocket.WebSocketServer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private MessageMapper msgMapper;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private CroupMessageReadStatusMapper readStatusMapper;
    @Autowired
    private RedisTemplate<String, Object> redis;
    @Autowired
    private BlockService blockService;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private RabbitTemplate rabbit;
    @Lazy
    @Autowired
    private WebSocketServer wsServer;
    @Autowired
    private SensitiveWordBs sensitiveWordBs;
    @Autowired
    private BloomFilter<String> bloomFilter;

    private static final String CONV_KEY = "conversations:";
    private static final String CHAT_KEY_PREFIX = "chat:";
    private static final String GROUP_CHAT_KEY_PREFIX = "group_chat:";
    private static final String BROADCAST_KEY = "broadcasts";
    private static final String OFFLINE_PREFIX = "offline:";
    private static final String GROUP_INFO_PREFIX = "group:";
    private static final String UNREAD_PRIVATE_PREFIX = "unread:private:";
    private static final String UNREAD_PRIVATE_TOTAL_PREFIX = "unread:private:total:";
    private static final String UNREAD_GROUP_PREFIX = "unread:group:";
    private static final String UNREAD_GROUP_TOTAL_PREFIX = "unread:group:total:";
    private static final String UNREAD_TOTAL_PREFIX = "unread:total:";

    private void delayDeleteRedisKey(final String key) {
        // 延迟删除
        redis.expire(key, 30, TimeUnit.SECONDS);  // 30秒后删除
        redis.delete(key);  // 立即删除
    }

    @Async
    @Override
    public void sendMessage(Message msg) throws JsonProcessingException {
        // 先进行敏感词过滤
        String original = msg.getContent();
        if (sensitiveWordBs.contains(original)) {
            // 将所有敏感词替换为 '*'
            String filtered = sensitiveWordBs.replace(original);
            msg.setContent(filtered);
        }
        msg.setMessageType("PRIVATE");

        // 1. 检查屏蔽
        boolean blocked = blockService.isBlocked(msg.getReceiverId(), msg.getSenderId());
        msg.setIsBlocked(blocked);
        if (blocked) {
            return;
        }
        // 2. 写 MySQL
        msgMapper.insert(msg);

        // 3. 更新 Redis 会话列表
        long score = msg.getTimestamp().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        redis.opsForZSet().add(CONV_KEY + msg.getSenderId(),
                msg.getReceiverId().toString(),
                score);
        redis.opsForZSet().add(CONV_KEY + msg.getReceiverId(),
                msg.getSenderId().toString(),
                score);

        // 4. 写 Redis 聊天记录
        Long u1 = Math.min(msg.getSenderId(), msg.getReceiverId());
        Long u2 = Math.max(msg.getSenderId(), msg.getReceiverId());
        String chatKey = CHAT_KEY_PREFIX + u1 + ":" + u2;
        String payload = json.writeValueAsString(msg);
        redis.opsForList().rightPush(chatKey, payload);

        // 5. 在线推送 or 离线缓存
        String recv = msg.getReceiverId().toString();
        if (wsServer.isOnline(msg.getReceiverId())) {
            wsServer.sendOneMessage(recv, payload);
        } else {
            redis.opsForList().rightPush(OFFLINE_PREFIX + recv, payload);
        }
        bloomFilter.put(chatKey);

        rabbit.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                payload
        );
        delayDeleteRedisKey(CHAT_KEY_PREFIX + u1 + ":" + u2);
    }

    @Override
    public List<Message> getHistory(Long u1, Long u2) {
        // Redis key
        String key = CHAT_KEY_PREFIX + Math.min(u1, u2) + ":" + Math.max(u1, u2);
        if (!bloomFilter.mightContain(key)) {
            return Collections.emptyList();
        }
        // 1. 从 Redis 读取
        List<Object> rawList = redis.opsForList().range(key, 0, -1);
        if (rawList != null && !rawList.isEmpty()) {

            return rawList.stream().map(o -> {
                try {
                    return json.readValue(o.toString(), Message.class);
                } catch (Exception e) {
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }
        // 2. 如果Redis 缓存空，回退到 MySQL
        List<Message> mysqlList = msgMapper.findChatHistory(u1, u2);
        if (mysqlList == null || mysqlList.isEmpty()) {
            return Collections.emptyList();
        }
        // 3. 将 MySQL 读到的历史缓存到 Redis
        mysqlList.forEach(msg -> {
            try {
                redis.opsForList().rightPush(key, json.writeValueAsString(msg));
            } catch (Exception ignore) {
            }
        });
        // 4. 返回 MySQL 数据
        return mysqlList;
    }

    @Override
    public List<Map<String, Object>> getConversations(Long userId) {
        Set<Object> others = redis.opsForZSet().reverseRange(CONV_KEY + userId, 0, -1);
        if (others == null) return Collections.emptyList();
        return others.stream().map(o -> {
            Long otherId = Long.valueOf(o.toString());
            Double score = redis.opsForZSet().score(CONV_KEY + userId, o);
            Map<String, Object> m = new HashMap<>();
            m.put("userId", otherId);
            m.put("lastTime", new Date(score.longValue()));
            return m;
        }).collect(Collectors.toList());
    }

    // =============== 群聊相关实现 ===============

    @Override
    public ChatGroup createGroup(String name, Long creatorId, String description) throws JsonProcessingException {
        // 1. 创建群组
        ChatGroup group = new ChatGroup();
        group.setName(name);
        group.setCreatorId(creatorId);
        group.setDescription(description);
        groupMapper.insertGroup(group);
        // 2. 添加创建者为群成员
        GroupMember creator = new GroupMember();
        creator.setGroupId(group.getId());
        creator.setUserId(creatorId);
        groupMapper.insertMember(creator);

        // 3. 缓存群组信息到Redis
        String groupKey = GROUP_INFO_PREFIX + group.getId();
        redis.opsForHash().put(groupKey, "name", group.getName());
        redis.opsForHash().put(groupKey, "creator", group.getCreatorId().toString());
        redis.opsForHash().put(groupKey, "description", group.getDescription());

        // 4. 发送消息到群
        Message systemMsg = new Message();
        systemMsg.setMessageType("GROUP");
        systemMsg.setGroupId(group.getId());
        systemMsg.setSenderId(0L);  // 系统消息发送者ID为0
        systemMsg.setContent("群聊 \"" + name + "\" 已创建");
        systemMsg.setType("text");
        sendGroupMessage(systemMsg);

        return group;
    }

    @Override
    public void addGroupMember(Long groupId, Long userId) throws JsonProcessingException {
        // 1. 检查用户是否已在群内
        List<Long> members = groupMapper.findGroupMemberIds(groupId);
        if (members.contains(userId)) {
            return;
        }

        // 2. 添加群成员
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        groupMapper.insertMember(member);

        // 3. 发送系统消息到群
        Message systemMsg = new Message();
        systemMsg.setMessageType("GROUP");
        systemMsg.setGroupId(groupId);
        systemMsg.setSenderId(0L);  // 系统消息发送者ID为0
        systemMsg.setContent("新成员加入群聊");
        systemMsg.setType("text");
        sendGroupMessage(systemMsg);
    }

    @Override
    public void removeGroupMember(Long groupId, Long userId) {
        groupMapper.removeMember(groupId, userId);
    }

    @Async
    @Override
    public void sendGroupMessage(Message msg) throws JsonProcessingException {
        // 先进行敏感词过滤
        String original = msg.getContent();
        if (sensitiveWordBs.contains(original)) {
            // 将所有敏感词替换为 '*'
            String filtered = sensitiveWordBs.replace(original);
            msg.setContent(filtered);
        }
        msg.setMessageType("GROUP");

        // 1. 写 MySQL
        msgMapper.insert(msg);

        // 2. 写 Redis 群聊记录
        String groupChatKey = GROUP_CHAT_KEY_PREFIX + msg.getGroupId();
        String payload = json.writeValueAsString(msg);
        redis.opsForList().rightPush(groupChatKey, payload);
        bloomFilter.put(groupChatKey);

        // 3. 获取群成员列表并使用 sendMoreMessage 发送消息
        List<Long> memberIds = groupMapper.findGroupMemberIds(msg.getGroupId());
        wsServer.sendMoreMessage(memberIds, payload, msg.getSenderId(), blockService, redis);

        // 4. RabbitMQ 推送给其他消费者或审计
        rabbit.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY + ".group",
                payload
        );
        delayDeleteRedisKey(GROUP_CHAT_KEY_PREFIX + msg.getGroupId());
    }

    @Override
    public List<Message> getGroupHistory(Long groupId) {
        // Redis key
        String key = GROUP_CHAT_KEY_PREFIX + groupId;
        if (!bloomFilter.mightContain(key)) {
            return Collections.emptyList();
        }
        // 1. 尝试从 Redis 读
        List<Object> rawList = redis.opsForList().range(key, 0, -1);
        if (rawList != null && !rawList.isEmpty()) {
            // 反序列化并返回
            return rawList.stream().map(o -> {
                try {
                    return json.readValue(o.toString(), Message.class);
                } catch (Exception e) {
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }
        // 2. Redis 缓存空，回退到 MySQL
        List<Message> mysqlList = msgMapper.findGroupChatHistory(groupId);
        if (mysqlList == null || mysqlList.isEmpty()) {
            return Collections.emptyList();
        }
        // 3. 将 MySQL 读到的历史缓存到 Redis
        mysqlList.forEach(msg -> {
            try {
                redis.opsForList().rightPush(key, json.writeValueAsString(msg));
            } catch (Exception ignore) {
            }
        });
        // 4. 返回 MySQL 数据
        return mysqlList;
    }

    @Override
    public List<ChatGroup> getUserGroups(Long userId) {
        return groupMapper.findGroupsByUserId(userId);
    }

    // =============== 广播消息相关实现 ===============

    @Async
    @Override
    public void sendBroadcast(Message msg) throws JsonProcessingException {
        // 设置消息类型为BROADCAST
        msg.setMessageType("BROADCAST");

        // 1. 写 MySQL
        msgMapper.insert(msg);

        // 2. 写 Redis 广播缓存
        String payload = json.writeValueAsString(msg);
        redis.opsForList().rightPush(BROADCAST_KEY, payload);
        redis.opsForList().trim(BROADCAST_KEY, 0, 99);
        bloomFilter.put(BROADCAST_KEY);

        // 3. 向所有在线用户推送
        wsServer.sendAllMessage(payload);

        // 4. RabbitMQ 推送给其他消费者或审计
        rabbit.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY + ".broadcast",
                payload
        );
    }

    @Override
    public List<Message> getRecentBroadcasts(int limit) {
        // 获取条数
        limit = Math.min(limit, 100);
        if (!bloomFilter.mightContain(BROADCAST_KEY)) {
            return Collections.emptyList();
        }
        // 1. 尝试从 Redis 读
        List<Object> rawList = redis.opsForList().range(BROADCAST_KEY, 0, limit - 1);
        if (rawList != null && !rawList.isEmpty()) {
            // 反序列化并返回
            return rawList.stream().map(o -> {
                try {
                    return json.readValue(o.toString(), Message.class);
                } catch (Exception e) {
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }

        // 2. Redis 缓存空，回退到 MySQL
        List<Message> mysqlList = msgMapper.findBroadcasts(limit);
        if (mysqlList == null || mysqlList.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 将 MySQL 读到的广播缓存到 Redis
        mysqlList.forEach(msg -> {
            try {
                redis.opsForList().rightPush(BROADCAST_KEY, json.writeValueAsString(msg));
            } catch (Exception ignore) {
            }
        });

        // 4. 返回 MySQL 数据
        return mysqlList;
    }
    // =============== 未读消息相关方法 ===============

    @Override
    public int getUnreadPrivateMessageCount(Long userId, Long senderId) {
        // 首先从Redis获取
        String key = UNREAD_PRIVATE_PREFIX + userId + ":" + senderId;
        Object value = redis.opsForValue().get(key);
        if (value != null) {
            return Integer.parseInt(value.toString());
        }

        // Redis中没有，从数据库查询
        int count = msgMapper.countUnreadPrivateMessages(userId, senderId);

        // 写入Redis缓存
        redis.opsForValue().set(key, count, 1, TimeUnit.DAYS);

        return count;
    }

    @Override
    public Map<String, Integer> getAllUnreadPrivateMessageCounts(Long userId) {
        // 尝试从Redis中获取
        String hashKey = UNREAD_PRIVATE_TOTAL_PREFIX + userId;
        Map<Object, Object> redisMap = redis.opsForHash().entries(hashKey);

        if (redisMap != null && !redisMap.isEmpty()) {
            // 转换为所需的Map<String, Integer>格式
            Map<String, Integer> result = new HashMap<>();
            for (Map.Entry<Object, Object> entry : redisMap.entrySet()) {
                result.put(entry.getKey().toString(),
                        Integer.parseInt(entry.getValue().toString()));
            }
            return result;
        }
        // Redis中没有，从数据库查询
        List<Map<String, Object>> dbCounts = msgMapper.countAllUnreadPrivateMessages(userId);
        Map<String, Integer> result = new HashMap<>();

        for (Map<String, Object> item : dbCounts) {
            Long senderId = (Long) item.get("sender_id");
            Integer count = ((Number) item.get("count")).intValue();
            redis.opsForHash().put(hashKey, senderId.toString(), count);
            result.put(senderId.toString(), count);
        }
        redis.expire(hashKey, 1, TimeUnit.DAYS);
        return result;
    }

    @Override
    @Transactional
    public void markPrivateMessagesAsRead(Long userId, Long senderId) {
        // 更新数据库中的已读状态
        msgMapper.markPrivateMessagesAsRead(userId, senderId);
        // 更新Redis中的未读计数器
        String unreadKey = UNREAD_PRIVATE_PREFIX + userId + ":" + senderId;
        String unreadTotalHashKey = UNREAD_PRIVATE_TOTAL_PREFIX + userId;
        String totalUnreadKey = UNREAD_TOTAL_PREFIX + userId;
        // 获取当前未读数
        Object currentUnread = redis.opsForValue().get(unreadKey);
        int unreadCount = currentUnread != null ? Integer.parseInt(currentUnread.toString()) : 0;
        // 重置用户的未读数
        redis.opsForValue().set(unreadKey, 0);
        // 从私聊总未读中删除
        redis.opsForHash().delete(unreadTotalHashKey, senderId.toString());
        // 减少总未读计数
        if (unreadCount > 0) {
            redis.opsForValue().decrement(totalUnreadKey, unreadCount);
        }
    }

    @Override
    public int getUnreadGroupMessageCount(Long userId, Long groupId) {
        // 首先从Redis获取
        String key = UNREAD_GROUP_PREFIX + userId + ":" + groupId;
        Object value = redis.opsForValue().get(key);
        if (value != null) {
            return Integer.parseInt(value.toString());
        }
        // Redis中没有，从数据库查询
        int count = readStatusMapper.countUnreadGroupMessages(userId, groupId);
        // 写入Redis缓存
        redis.opsForValue().set(key, count, 1, TimeUnit.DAYS);
        return count;
    }

    @Override
    public Map<String, Integer> getAllUnreadGroupMessageCounts(Long userId) {
        // 尝试从Redis中获取
        String hashKey = UNREAD_GROUP_TOTAL_PREFIX + userId;
        Map<Object, Object> redisMap = redis.opsForHash().entries(hashKey);
        if (redisMap != null && !redisMap.isEmpty()) {
            // 转换为所需的Map<String, Integer>格式
            Map<String, Integer> result = new HashMap<>();
            for (Map.Entry<Object, Object> entry : redisMap.entrySet()) {
                result.put(entry.getKey().toString(),
                        Integer.parseInt(entry.getValue().toString()));
            }
            return result;
        }
        // Redis中没有，从数据库查询
        List<Map<String, Object>> dbCounts = readStatusMapper.countAllUnreadGroupMessages(userId);
        Map<String, Integer> result = new HashMap<>();
        for (Map<String, Object> item : dbCounts) {
            Long groupId = (Long) item.get("group_id");
            Integer count = ((Number) item.get("count")).intValue();
            // 同时更新Redis
            redis.opsForHash().put(hashKey, groupId.toString(), count);
            result.put(groupId.toString(), count);
        }
        redis.expire(hashKey, 1, TimeUnit.DAYS);
        return result;
    }

    @Override
    @Transactional
    public void markGroupMessagesAsRead(Long userId, Long groupId) {
        // 获取该用户在群组中的未读消息
        List<Message> unreadMessages = msgMapper.findUnreadGroupMessages(userId, groupId);
        // 更新数据库中的已读状态
        for (Message msg : unreadMessages) {
            readStatusMapper.markAsRead(msg.getId(), userId);
        }
        // 更新Redis中的未读计数器
        String unreadKey = UNREAD_GROUP_PREFIX + userId + ":" + groupId;
        String unreadTotalHashKey = UNREAD_GROUP_TOTAL_PREFIX + userId;
        String totalUnreadKey = UNREAD_TOTAL_PREFIX + userId;
        // 获取当前未读数，用于后续减少总未读数
        Object currentUnread = redis.opsForValue().get(unreadKey);
        int unreadCount = currentUnread != null ? Integer.parseInt(currentUnread.toString()) : 0;
        // 重置特定群组的未读数
        redis.opsForValue().set(unreadKey, 0);
        // 从群聊总未读中删除
        redis.opsForHash().delete(unreadTotalHashKey, groupId.toString());
        // 减少总未读计数
        if (unreadCount > 0) {
            redis.opsForValue().decrement(totalUnreadKey, unreadCount);
        }
    }

    @Override
    public int getTotalUnreadMessageCount(Long userId) {
        // 首先从Redis获取
        String key = UNREAD_TOTAL_PREFIX + userId;
        Object value = redis.opsForValue().get(key);
        if (value != null) {
            return Integer.parseInt(value.toString());
        }
        // Redis中没有，计算私聊和群聊未读总和
        Map<String, Integer> privateUnreads = getAllUnreadPrivateMessageCounts(userId);
        Map<String, Integer> groupUnreads = getAllUnreadGroupMessageCounts(userId);
        int total = 0;
        // 计算私聊未读总数
        for (Integer count : privateUnreads.values()) {
            total += count;
        }
        // 计算群聊未读总数
        for (Integer count : groupUnreads.values()) {
            total += count;
        }
        // 缓存到Redis
        redis.opsForValue().set(key, total, 1, TimeUnit.DAYS);
        return total;
    }
}