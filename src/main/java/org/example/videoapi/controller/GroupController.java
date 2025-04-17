package org.example.videoapi.controller;

import org.example.videoapi.pojo.dto.ResultResponse;
import org.example.videoapi.pojo.entity.ChatGroup;
import org.example.videoapi.pojo.entity.Message;
import org.example.videoapi.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group")
public class GroupController {
    @Autowired
    private ChatService chatService;

    @PostMapping("/create")
    public ResultResponse<ChatGroup> createGroup(
            @RequestParam String name,
            @RequestParam Long creatorId,
            @RequestParam(required = false) String description) {
        try {
            ChatGroup group = chatService.createGroup(name, creatorId, description);
            return ResultResponse.success("群组创建成功", group);
        } catch (Exception e) {
            return ResultResponse.error(400,"群组创建失败: ");
        }
    }

    @PostMapping("/join")
    public ResultResponse<String> joinGroup(
            @RequestParam Long groupId,
            @RequestParam Long userId) {
        try {
            chatService.addGroupMember(groupId, userId);
            return ResultResponse.success("加入群组成功", null);
        } catch (Exception e) {
            return ResultResponse.error(400,"加入群组失败: " );
        }
    }

    @PostMapping("/leave")
    public ResultResponse<String> leaveGroup(
            @RequestParam Long groupId,
            @RequestParam Long userId) {
        chatService.removeGroupMember(groupId, userId);
        return ResultResponse.success("退出群组成功", null);
    }

    @GetMapping("/list/{userId}")
    public ResultResponse<List<ChatGroup>> getUserGroups(@PathVariable Long userId) {
        List<ChatGroup> groups = chatService.getUserGroups(userId);
        return ResultResponse.success("用户群组列表", groups);
    }

    @GetMapping("/history/{groupId}")
    public ResultResponse<List<Message>> getGroupHistory(@PathVariable Long groupId) {
        List<Message> history = chatService.getGroupHistory(groupId);
        return ResultResponse.success("群聊记录", history);
    }

    @PostMapping("/message")
    public ResultResponse<String> sendGroupMessage(@RequestBody Message message) {
        try {
            chatService.sendGroupMessage(message);
            return ResultResponse.success("发送成功", null);
        } catch (Exception e) {
            return ResultResponse.error(400,"发送失败: ");
        }
    }
}