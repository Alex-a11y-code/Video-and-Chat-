package org.example.videoapi.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface CommentLikesMapper {
    @Select("SELECT COUNT(*) > 0 FROM comment_likes WHERE user_id = #{userId} AND video_comment_id = #{videoCommentId}")
    boolean isLiked(@Param("userId") Long userId, @Param("videoCommentId") Long videoCommentId);

    @Insert("INSERT INTO comment_likes (user_id, video_comment_id) VALUES (#{userId}, #{videoCommentId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void saveCommentLikes(@Param("userId") Long userId, @Param("videoCommentId") Long videoCommentId);

    @Delete("DELETE FROM comment_likes WHERE user_id = #{userId} AND video_comment_id = #{videoCommentId}")
    void removeCommentLikes(@Param("userId") Long userId, @Param("videoCommentId") Long videoCommentId);
}
