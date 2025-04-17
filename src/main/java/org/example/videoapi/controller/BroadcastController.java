package org.example.videoapi.controller;

import org.example.videoapi.pojo.dto.ResultResponse;
import org.example.videoapi.pojo.entity.Message;
import org.example.videoapi.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/broadcast")
public class BroadcastController {
    @Lazy
    @Autowired
    private ChatService chatService;

    /**
     * 发送广播信息
     */
    @PostMapping("/send")
    public ResultResponse<String> sendBroadcast(@RequestBody Message message) {
        try {
            chatService.sendBroadcast(message);
            return ResultResponse.success("广播发送成功", null);
        } catch (Exception e) {
            return ResultResponse.error(400, "广播发送失败: ");
        }
    }

    /**
     * 获取最近广播信息
     */
    @GetMapping("/recent")
    public ResultResponse<List<Message>> getRecentBroadcasts(
            @RequestParam(defaultValue = "20") int limit) {
        List<Message> broadcasts = chatService.getRecentBroadcasts(limit);
        return ResultResponse.success("最近广播", broadcasts);
    }
}