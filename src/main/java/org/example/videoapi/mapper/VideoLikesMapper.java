package org.example.videoapi.mapper;

import org.apache.ibatis.annotations.*;
import org.example.videoapi.pojo.entity.VideoLikes;

@Mapper
public interface VideoLikesMapper {
    @Select("SELECT COUNT(*) > 0 FROM video_likes WHERE user_id = #{userId} AND video_id = #{videoId}")
    boolean isLiked(@Param("userId") Long userId, @Param("videoId") Long videoId);

    @Insert("INSERT INTO video_likes (user_id, video_id) VALUES (#{userId}, #{videoId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void saveVideoLikes(@Param("userId") Long userId, @Param("videoId") Long videoId);

    @Delete("DELETE FROM video_likes WHERE user_id = #{userId} AND video_id = #{videoId}")
    void removeVideoLikes(@Param("userId") Long userId, @Param("videoId") Long videoId);
}
