package org.example.videoapi.controller;

import org.example.videoapi.pojo.dto.ResultResponse;
import org.example.videoapi.service.CommentLikesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/commentLikes")
public class CommentLikesController {

    @Autowired
    private CommentLikesService commentLikesService;

    /**
     *点赞
     */
    @PostMapping("save")
    public ResultResponse<String> likeComment(@RequestParam Long userId, @RequestParam Long videoCommentId) {
        commentLikesService.saveCommentLikes(userId, videoCommentId);
        return ResultResponse.success("点赞成功", null);
    }
    /**
     *取消点赞
     */

    @DeleteMapping("remove")
    public ResultResponse<String> unlikeComment(@RequestParam Long userId, @RequestParam Long videoCommentId) {
        commentLikesService.removeCommentLikes(userId, videoCommentId);
        return ResultResponse.success("取消点赞成功", null);
    }
}
