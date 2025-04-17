package org.example.videoapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.videoapi.pojo.entity.ChatGroup;
import org.example.videoapi.pojo.entity.Message;
import java.util.List;
import java.util.Map;

public interface ChatService {

    void sendMessage(Message msg) throws JsonProcessingException;
    List<Message> getHistory(Long u1, Long u2);
    List<Map<String,Object>> getConversations(Long userId);


    ChatGroup createGroup(String name, Long creatorId, String description) throws JsonProcessingException;
    void addGroupMember(Long groupId, Long userId) throws JsonProcessingException;
    void removeGroupMember(Long groupId, Long userId);
    void sendGroupMessage(Message msg) throws JsonProcessingException;
    List<Message> getGroupHistory(Long groupId);
    List<ChatGroup> getUserGroups(Long userId);


    void sendBroadcast(Message msg) throws JsonProcessingException;
    List<Message> getRecentBroadcasts(int limit);


    int getUnreadPrivateMessageCount(Long userId, Long senderId);
    Map<String, Integer> getAllUnreadPrivateMessageCounts(Long userId);
    void markPrivateMessagesAsRead(Long userId, Long senderId);


    int getUnreadGroupMessageCount(Long userId, Long groupId);
    Map<String, Integer> getAllUnreadGroupMessageCounts(Long userId);
    void markGroupMessagesAsRead(Long userId, Long groupId);


    int getTotalUnreadMessageCount(Long userId);
}