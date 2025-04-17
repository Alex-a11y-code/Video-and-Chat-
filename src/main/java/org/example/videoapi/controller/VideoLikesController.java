package org.example.videoapi.controller;

import org.example.videoapi.pojo.dto.ResultResponse;
import org.example.videoapi.service.VideoLikesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videoLikes")
public class VideoLikesController {
    @Autowired
    private VideoLikesService videoLikesService;
    /**
     *点赞
     */

    @PostMapping("/save")
    public ResultResponse<String> saveVideoLikes(@RequestParam Long userId, @RequestParam Long videoId) {
        videoLikesService.saveVideoLikes(userId, videoId);
        return ResultResponse.success("点赞成功", null);
    }
    /**
     *取消点赞
     */

    @PostMapping("/remove")
    public ResultResponse<String> removeVideoLikes(@RequestParam Long userId, @RequestParam Long videoId) {
        videoLikesService.removeVideoLikes(userId, videoId);
        return ResultResponse.success("取消点赞成功", null);
    }
}
