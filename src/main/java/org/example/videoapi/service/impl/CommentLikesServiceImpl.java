package org.example.videoapi.service.impl;

import org.example.videoapi.exception.ApiException;
import org.example.videoapi.mapper.CommentLikesMapper;
import org.example.videoapi.mapper.CommentMapper;
import org.example.videoapi.service.CommentLikesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class CommentLikesServiceImpl implements CommentLikesService {
    @Autowired
    private CommentLikesMapper commentLikesMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public void saveCommentLikes(Long userId, Long videoCommentId) {
        // 如果没有点赞过，则进行点赞操作
        if (!commentLikesMapper.isLiked(userId, videoCommentId)) {
            commentLikesMapper.saveCommentLikes(userId, videoCommentId);
            // 点赞成功后，更新评论点赞数加一
            commentMapper.incrementCommentLikeCount(videoCommentId);
        } else {
            throw new ApiException("您已经点赞过了", 400);
        }
    }

    @Override
    public void removeCommentLikes(Long userId, Long videoCommentId) {
        // 如果已经点赞过，则允许取消点赞
        if (commentLikesMapper.isLiked(userId, videoCommentId)) {
            commentLikesMapper.removeCommentLikes(userId, videoCommentId);
            // 取消点赞后，更新评论点赞数减一
            commentMapper.decrementCommentLikeCount(videoCommentId);
        } else {
            throw new ApiException("您没有点赞过", 400);
        }
    }
}
