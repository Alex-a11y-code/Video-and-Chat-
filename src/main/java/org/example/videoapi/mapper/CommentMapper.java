package org.example.videoapi.mapper;
import org.apache.ibatis.annotations.*;
import org.example.videoapi.pojo.entity.Comment;
import org.example.videoapi.pojo.vo.CommentVO;
import java.util.List;

@Mapper
public interface CommentMapper {
    //添加评论
    @Insert("INSERT INTO comment (content, user_id, video_id, root_comment_id, create_time, update_time) VALUES (#{content}, #{userId}, #{videoId}, #{rootCommentId}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "videoCommentId")
    int saveComment(Comment comment);
    //根据id删除评论
    @Update("UPDATE comment SET isDelete = 1 WHERE video_comment_id = #{videoCommentId} OR root_comment_id = #{videoCommentId}")
    int removeCommentById(Long videoCommentId);
    //保存回复评论
    @Insert("INSERT INTO comment (content, user_id, video_id, root_comment_id, to_comment_id, create_time, update_time) VALUES (#{content}, #{userId}, #{videoId}, #{rootCommentId}, #{toCommentId}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "videoCommentId")
    int saveToComment(Comment comment);
    //更新评论内容
    @Update("UPDATE comment SET content = #{content}, root_comment_id = #{rootCommentId}, to_comment_id = #{toCommentId}, update_time = #{updateTime} WHERE video_comment_id = #{videoCommentId}")
    int updateComment(Comment comment);
    // 分页查询顶级评论，并统计每个顶级评论的子评论数量
    @Select("SELECT c.video_comment_id, c.content, c.user_id, c.video_id, c.root_comment_id, c.comment_like_count AS commentLikeCount, c.create_time, c.update_time, (SELECT COUNT(*) FROM comment WHERE root_comment_id = c.video_comment_id AND isDelete = 0) AS childCount FROM comment c WHERE c.video_id = #{videoId} AND c.root_comment_id IS NULL AND c.isDelete = 0 ORDER BY c.create_time DESC LIMIT #{offset}, #{limit}")
    List<CommentVO> getRootComments(@Param("videoId") Long videoId, @Param("offset") int offset, @Param("limit") int limit);

    // 根据顶级评论 id 列表，获取各自按时间升序排序的前三条子评论
    @Select("<script> SELECT video_comment_id, content, user_id, video_id, root_comment_id, to_comment_id, comment_like_count AS commentLikeCount, create_time, update_time FROM ( SELECT c.video_comment_id, c.content, c.user_id, c.video_id, c.root_comment_id, c.to_comment_id, c.comment_like_count, c.create_time, c.update_time, ROW_NUMBER() OVER (PARTITION BY c.root_comment_id ORDER BY c.video_comment_id ASC) AS rn FROM comment c WHERE c.root_comment_id IN <foreach collection='rootCommentIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> AND c.isDelete = 0 ) t WHERE t.rn &lt;= 3 </script>")

    List<Comment> getTopThreeChildComments(@Param("rootCommentIds") List<Long> rootCommentIds);

    // 根据顶级评论 id 查询所有子评论
    @Select("SELECT video_comment_id, content, user_id, video_id, root_comment_id, to_comment_id, comment_like_count AS commentLikeCount, create_time, update_time FROM comment WHERE root_comment_id = #{rootCommentId} AND isDelete = 0 ORDER BY create_time ASC LIMIT #{offset}, #{limit}")
    List<Comment> getChildCommentsByRootId(@Param("rootCommentId") Long rootCommentId, @Param("offset") int offset, @Param("limit") int limit);
    // 点赞时评论点赞数加一
    @Update("UPDATE comment SET comment_like_count = comment_like_count + 1 WHERE video_comment_id = #{videoCommentId}")
    void incrementCommentLikeCount(@Param("videoCommentId") Long videoCommentId);
    // 取消点赞时评论点赞数减一，同时确保点赞数不会为负
    @Update("UPDATE comment SET comment_like_count = CASE WHEN comment_like_count > 0 THEN comment_like_count - 1 ELSE 0 END WHERE video_comment_id = #{videoCommentId}")
    void decrementCommentLikeCount(@Param("videoCommentId") Long videoCommentId);
}
