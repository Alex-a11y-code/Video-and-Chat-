package org.example.videoapi.service;
import org.example.videoapi.pojo.entity.Comment;
import org.example.videoapi.pojo.vo.CommentVO;

import java.util.List;

/*
  评论服务接口
 */
public interface CommentService {
    void saveComment(Comment comment);//添加评论
    void deleteComment(Long videoCommentId);//删除评论
    void saveToComment(Comment comment, Long rootCommentId);//添加回复评论
    void updateComment(Comment comment, Long videoCommentId);//更新评论内容
    // 获取指定视频的顶级评论
    List<CommentVO> getTopLevelComments(Long videoId, int page, int size);
    // 根据根评论 id 分页获取所有子评论
    List<Comment> getChildCommentsByRootId(Long rootCommentId, int page, int size);
}
