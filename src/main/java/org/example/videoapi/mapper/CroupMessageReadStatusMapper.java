package org.example.videoapi.mapper;

import org.apache.ibatis.annotations.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface CroupMessageReadStatusMapper {

    //更新消息阅读状态
    @Insert("INSERT INTO message_read_status(message_id, user_id, is_read, read_time) VALUES(#{messageId}, #{userId}, TRUE, NOW()) ON DUPLICATE KEY UPDATE is_read=TRUE, read_time=NOW()")
    int markAsRead(@Param("messageId") Long messageId, @Param("userId") Long userId);

    // 统计指定群组中已读的群聊消息数量
    @Select("SELECT COUNT(*) FROM message m WHERE m.group_id = #{groupId} AND m.message_type = 'GROUP' AND m.sender_id != #{userId} AND NOT EXISTS (SELECT 1 FROM message_read_status rs WHERE rs.message_id = m.id AND rs.user_id = #{userId} AND rs.is_read = TRUE)")
    int countUnreadGroupMessages(@Param("userId") Long userId, @Param("groupId") Long groupId);

    // 统计用户在所有群组中，每个群组中未标记为已读的群聊消息数量，按群组分组返回
    @Select("SELECT m.group_id, COUNT(*) AS count FROM message m WHERE m.message_type = 'GROUP' AND m.sender_id != #{userId} AND NOT EXISTS (SELECT 1 FROM message_read_status rs WHERE rs.message_id = m.id AND rs.user_id = #{userId} AND rs.is_read = TRUE) GROUP BY m.group_id")
    List<Map<String, Object>> countAllUnreadGroupMessages(@Param("userId") Long userId);
}