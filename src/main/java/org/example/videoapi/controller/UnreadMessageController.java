package org.example.videoapi.controller;

import org.example.videoapi.pojo.dto.ResultResponse;
import org.example.videoapi.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat/unread")
public class UnreadMessageController {
    @Autowired
    private ChatService chatService;

    @GetMapping("/private/{userId}/{senderId}")
    public ResultResponse<Integer> getUnreadPrivateCount(
            @PathVariable Long userId,
            @PathVariable Long senderId) {
        int count = chatService.getUnreadPrivateMessageCount(userId, senderId);
        return ResultResponse.success("未读私聊消息数", count);
    }

    @GetMapping("/private/all/{userId}")
    public ResultResponse<Map<String, Integer>> getAllUnreadPrivateCounts(
            @PathVariable Long userId) {
        Map<String, Integer> counts = chatService.getAllUnreadPrivateMessageCounts(userId);
        return ResultResponse.success("所有未读私聊消息数", counts);
    }

    @PostMapping("/private/read")
    public ResultResponse<String> markPrivateRead(
            @RequestParam Long userId,
            @RequestParam Long senderId) {
        chatService.markPrivateMessagesAsRead(userId, senderId);
        return ResultResponse.success("已标记为已读", null);
    }

    @GetMapping("/group/{userId}/{groupId}")
    public ResultResponse<Integer> getUnreadGroupCount(
            @PathVariable Long userId,
            @PathVariable Long groupId) {
        int count = chatService.getUnreadGroupMessageCount(userId, groupId);
        return ResultResponse.success("未读群聊消息数", count);
    }

    @GetMapping("/group/all/{userId}")
    public ResultResponse<Map<String, Integer>> getAllUnreadGroupCounts(
            @PathVariable Long userId) {
        Map<String, Integer> counts = chatService.getAllUnreadGroupMessageCounts(userId);
        return ResultResponse.success("所有未读群聊消息数", counts);
    }
}