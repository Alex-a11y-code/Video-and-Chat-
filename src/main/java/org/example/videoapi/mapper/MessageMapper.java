package org.example.videoapi.mapper;

import org.apache.ibatis.annotations.*;
import org.example.videoapi.pojo.entity.Message;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface MessageMapper {

    // 插入一条新消息记录
    @Insert("INSERT INTO message(sender_id, receiver_id, content, type, message_type, group_id) VALUES(#{senderId}, #{receiverId}, #{content}, #{type}, #{messageType}, #{groupId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Message msg);

    // 查询两位用户之间的私聊记录，按时间顺序排序
    @Select("SELECT * FROM message WHERE (sender_id=#{u1} AND receiver_id=#{u2}) OR (sender_id=#{u2} AND receiver_id=#{u1}) AND message_type='PRIVATE' ORDER BY timestamp")
    List<Message> findChatHistory(@Param("u1") Long u1, @Param("u2") Long u2);

    // 查询指定群组的聊天记录（群聊），按时间顺序排序
    @Select("SELECT * FROM message WHERE group_id=#{groupId} AND message_type='GROUP' ORDER BY timestamp")
    List<Message> findGroupChatHistory(@Param("groupId") Long groupId);

    // 查询广播消息，按时间降序排列
    @Select("SELECT * FROM message WHERE message_type='BROADCAST' ORDER BY timestamp DESC LIMIT #{limit}")
    List<Message> findBroadcasts(@Param("limit") int limit);

    // 统计某用户接收到指定发送者的未读私聊消息数量
    @Select("SELECT COUNT(*) FROM message WHERE receiver_id=#{userId} AND sender_id=#{senderId} AND message_type='PRIVATE' AND is_read=FALSE")
    int countUnreadPrivateMessages(@Param("userId") Long userId, @Param("senderId") Long senderId);

    // 统计某用户所有来自不同发送者的未读私聊消息数量，并按发送者分组
    @Select("SELECT sender_id, COUNT(*) AS count FROM message WHERE receiver_id=#{userId} AND message_type='PRIVATE' AND is_read=FALSE GROUP BY sender_id")
    List<Map<String, Object>> countAllUnreadPrivateMessages(@Param("userId") Long userId);

    // 将所有未读的私聊消息标记为已读
    @Update("UPDATE message SET is_read=TRUE WHERE receiver_id=#{userId} AND sender_id=#{senderId} AND message_type='PRIVATE' AND is_read=FALSE")
    int markPrivateMessagesAsRead(@Param("userId") Long userId, @Param("senderId") Long senderId);


    // 查询指定群组中用户未读的群聊消息
    @Select("SELECT m.* FROM message m LEFT JOIN message_read_status mrs ON m.id = mrs.message_id AND mrs.user_id = #{userId} WHERE m.group_id = #{groupId} AND m.message_type = 'GROUP' AND (mrs.is_read IS NULL OR mrs.is_read = FALSE) AND m.sender_id != #{userId}")
    List<Message> findUnreadGroupMessages(@Param("userId") Long userId, @Param("groupId") Long groupId);

}
