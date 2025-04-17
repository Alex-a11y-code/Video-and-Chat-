package org.example.videoapi.controller;

import org.example.videoapi.pojo.vo.CommentVO;
import org.example.videoapi.pojo.entity.Comment;
import org.example.videoapi.pojo.dto.ResultResponse;
import org.example.videoapi.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/{videoId}")
    public ResultResponse<String> saveComment(@RequestBody Comment comment, @PathVariable Long videoId) {
        comment.setVideoId(videoId);
       commentService.saveComment(comment);
        return ResultResponse.success("评论添加成功",null);
    }

    @DeleteMapping("/{videoCommentId}")
    public ResultResponse<String> deleteComment(@PathVariable Long videoCommentId) {
        commentService.deleteComment(videoCommentId);
        return ResultResponse.success("评论删除成功",null);
    }

    @PostMapping("/{videoCommentId}/to/{rootCommentId}")
    public ResultResponse<String> saveToComment(@RequestBody Comment comment, @PathVariable Long videoCommentId, @PathVariable Long rootCommentId) {
        comment.setVideoCommentId(videoCommentId);
        comment.setRootCommentId(rootCommentId);
        commentService.saveToComment(comment, rootCommentId);
        return ResultResponse.success("回复评论添加成功",null);
    }

    @PutMapping("/{videoCommentId}")
    public ResultResponse<String> updateComment(@RequestBody Comment comment, @PathVariable Long videoCommentId) {
        comment.setVideoCommentId(videoCommentId);
        commentService.updateComment(comment, videoCommentId);
        return ResultResponse.success("评论更新成功",null);
    }

    // 获取指定视频的顶级评论（附带子评论数量和前三条回复）
    @GetMapping("/top/{videoId}")
    public ResultResponse<List<CommentVO>> getTopLevelComments(@PathVariable Long videoId,
                                                               @RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "10") int size) {
        List<CommentVO> comments = commentService.getTopLevelComments(videoId, page, size);
        return ResultResponse.success("获取顶级评论成功",comments);
    }

    // 分页获取指定顶级评论下的所有子评论
    @GetMapping("/child/{rootCommentId}")
    public ResultResponse<List<Comment>> getChildComments(@PathVariable Long rootCommentId,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        List<Comment> childComments = commentService.getChildCommentsByRootId(rootCommentId, page, size);
        return ResultResponse.success("获取所有子评论成功",childComments);
    }
}
