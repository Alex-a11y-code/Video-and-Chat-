package org.example.videoapi.service.impl;

import org.example.videoapi.mapper.CommentMapper;
import org.example.videoapi.pojo.entity.Comment;
import org.example.videoapi.pojo.vo.CommentVO;
import org.example.videoapi.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/*
评论服务实现类
 */
@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Override
    public void saveComment(Comment comment) {
        commentMapper.saveComment(comment);

    }

    @Override
    public void deleteComment(Long videoCommentId) {

        commentMapper.removeCommentById(videoCommentId);

    }

    @Override
    public void saveToComment(Comment comment, Long rootCommentId) {
        comment.setRootCommentId(rootCommentId);
        commentMapper.saveToComment(comment);

    }

    @Override
    public void updateComment(Comment comment, Long videoCommentId) {
        comment.setVideoCommentId(videoCommentId);
        commentMapper.updateComment(comment);

    }

    @Override
    public List<CommentVO> getTopLevelComments(Long videoId, int page, int size) {
        int offset = (page - 1) * size;
        // 查询顶级评论
        List<CommentVO> topComments = commentMapper.getRootComments(videoId, offset, size);
        if (topComments == null || topComments.isEmpty()) {
            return topComments;
        }
        // 提取所有顶级评论的 id，
        List<Long> topCommentIds = topComments.stream()
                .map(CommentVO::getVideoCommentId)
                .collect(Collectors.toList());
        // 查询每个顶级评论下的前三条子评论
        List<Comment> topThreeChildComments = commentMapper.getTopThreeChildComments(topCommentIds);
        // 根据 rootCommentId 分组后设置到各个顶级评论的 childComments 字段
        topComments.forEach(vo -> {
            List<Comment> childList = topThreeChildComments.stream()
                    .filter(c -> c.getRootCommentId() != null && c.getRootCommentId().equals(vo.getVideoCommentId()))
                    .collect(Collectors.toList());
            vo.setChildComments(childList);
        });
        return topComments;

    }

    @Override
    public List<Comment> getChildCommentsByRootId(Long rootCommentId, int page, int size) {
        int offset = (page - 1) * size;
        return commentMapper.getChildCommentsByRootId(rootCommentId, offset, size);
    }

}
