package org.example.videoapi.controller;

import org.example.videoapi.config.AliOssProperties;
import org.example.videoapi.pojo.dto.ResultResponse;
import org.example.videoapi.pojo.entity.Message;
import org.example.videoapi.service.BlockService;
import org.example.videoapi.service.ChatService;
import org.example.videoapi.util.AliOssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private AliOssProperties aliOssProps;

    /**
     * 屏蔽用户
     */
    @PostMapping("/block")
    public ResultResponse<String> block(@RequestParam Long userId, @RequestParam Long targetId) {
        blockService.block(userId, targetId);
        return ResultResponse.success("已屏蔽", null);
    }

    /**
     * 解除屏蔽
     */
    @PostMapping("/unblock")
    public ResultResponse<String> unblock(@RequestParam Long userId, @RequestParam Long targetId) {
        blockService.unblock(userId, targetId);
        return ResultResponse.success("已解除屏蔽", null);
    }

    /**
     * 会话列表
     */
    @GetMapping("/conversations/{userId}")
    public ResultResponse<List<Map<String, Object>>> convs(@PathVariable Long userId) {
        return ResultResponse.success("会话列表", chatService.getConversations(userId));
    }

    /**
     * 聊天历史
     */
    @GetMapping("/history")
    public ResultResponse<List<Message>> history(
            @RequestParam Long u1, @RequestParam Long u2) {
        return ResultResponse.success("聊天记录", chatService.getHistory(u1, u2));
    }

    /**
     * 上传聊天图片，返回 OSS URL
     */
    @PostMapping("/uploadImage")
    public ResultResponse<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String dir = aliOssProps.getChatDir();
        String url = aliOssUtil.uploadFile(file, dir);
        return ResultResponse.success("上传成功", url);
    }
}

