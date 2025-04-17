package org.example.videoapi.websocket;

import com.alibaba.fastjson.JSONObject;
import com.google.common.hash.BloomFilter;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.example.videoapi.pojo.entity.Message;
import org.example.videoapi.service.BlockService;
import org.example.videoapi.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Slf4j
@ServerEndpoint("/webSocketServer/{UserId}")
public class WebSocketServer {
    private static ChatService chatService;
    @Autowired
    public void setChatService(@Lazy ChatService cs) {
        chatService = cs;
    }
    @Autowired
     private RedisTemplate<String,Object> redis;
    @Autowired
    private BloomFilter<String> offlineMessageBloomFilter;
    /**
     * 与客户端的连接会话，需要通过他来给客户端发消息
     */
    private Session session;

    /**
     * 当前用户ID
     */
    private String userId;

    /**
     *  concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */
    private static CopyOnWriteArraySet<WebSocketServer> webSockets =new CopyOnWriteArraySet<>();

    /**
     *用来存在线连接用户信息
     */
    private static ConcurrentHashMap<String,Session> sessionPool = new ConcurrentHashMap<String,Session>();

    /**
     * 连接成功方法
     * @param session 连接会话
     * @param userId 用户编号
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        try {
            this.session = session;
            this.userId = userId;
            webSockets.add(this);
            sessionPool.put(userId, session);
            log.info("【websocket消息】 用户：" + userId + " 加入连接...");

            // 处理离线消息
            String offlineKey = "offline:" + userId;
            if (offlineMessageBloomFilter.mightContain(userId)) {
                List<Object> offlineMessages = redis.opsForList().range(offlineKey, 0, -1);

                if (offlineMessages != null && !offlineMessages.isEmpty()) {
                    for (Object msg : offlineMessages) {
                        // 发送离线消息
                        session.getAsyncRemote().sendText(msg.toString());
                    }

                    // 清空离线消息队列
                    redis.delete(offlineKey);
                }
            }
        } catch (Exception e) {
            log.error("---------------WebSocket连接异常---------------", e);
        }
    }

    /**
     * 关闭连接
     */
    @OnClose
    public void onClose(){
        try {
            webSockets.remove(this);
            sessionPool.remove(this.userId);
            log.info("【websocket消息】 用户："+ this.userId + " 断开连接...");
        } catch (Exception e) {
            log.error("---------------WebSocket断开异常---------------");
        }
    }
    @OnMessage
    public void onMessage(@PathParam("userId") String userId, String body)throws Exception {
        JSONObject jo = JSONObject.parseObject(body);
        Message msg = new Message();
        msg.setSenderId(Long.valueOf(userId));
        msg.setReceiverId(jo.getLong("targetUserId"));
        msg.setContent(jo.getString("message"));
        msg.setType(jo.getString("type")); // "text" 或 "image"
        msg.setTimestamp(LocalDateTime.now());
        chatService.sendMessage(msg);
    }
    /**
     * 此为广播消息
     * @param message
     */
    public void sendAllMessage(String message) {
        log.info("【websocket消息】广播消息:"+message);
        for(WebSocketServer webSocket : webSockets) {
            try {
                if(webSocket.session.isOpen()) {
                    webSocket.session.getAsyncRemote().sendText(message);
                }
            } catch (Exception e) {
                log.error("---------------WebSocket消息广播异常---------------");
            }
        }
    }

    /**
     * 单点消息
     * @param userId
     * @param message
     */
    public void sendOneMessage(String userId, String message) {
        Session session = sessionPool.get(userId);
        if (session != null&&session.isOpen()) {
            try {
                log.info("【websocket消息】 单点消息:"+message);
                session.getAsyncRemote().sendText(message);
            } catch (Exception e) {
                log.error("---------------WebSocket单点消息发送异常---------------");
            }
        }
    }

    /**
     * 发送多人单点消息，处理在线和离线用户
     * @param userIds 用户ID列表
     * @param message 消息内容
     */
    public void sendMoreMessage(List<Long> userIds, String message,
                                Long senderId,
                                BlockService blockService,
                                RedisTemplate<String, Object> redisTemplate) {
        final String OFFLINE_PREFIX = "offline:";

        for(Long userId : userIds) {
            // 跳过发送者自己
            if (userId.equals(senderId)) {
                continue;
            }

            // 检查是否被屏蔽
            if (blockService.isBlocked(userId, senderId)) {
                continue;
            }

            // 处理在线和离线消息
            Session session = sessionPool.get(userId.toString());
            if (session != null && session.isOpen()) {
                try {
                    log.info("【websocket消息】 群发单点消息:" + message);
                    session.getAsyncRemote().sendText(message);
                } catch (Exception e) {
                    log.error("---------------WebSocket多人单点消息发送异常---------------", e);
                }
            } else {
                // 用户离线，存储到离线消息队列
                redisTemplate.opsForList().rightPush(OFFLINE_PREFIX + userId, message);
                offlineMessageBloomFilter.put(userId.toString());
            }
        }
    }
    public boolean isOnline(Long userId) {
        Session s = sessionPool.get(userId.toString());
        return s != null && s.isOpen();
    }
}
