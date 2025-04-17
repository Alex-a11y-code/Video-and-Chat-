package org.example.videoapi.mapper;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Mapper;
import org.example.videoapi.pojo.entity.Video;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoMapper {
    //添加视频
    @Insert("INSERT INTO video (user_id,title,introduction,views_count,likes_count,surface_picture,video_address,category,status,create_time) VALUES (#{userId},#{title},#{introduction},#{viewsCount},#{likesCount},#{surfacePicture},#{videoAddress},#{category},#{status},NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "videoId")
    int saveVideo(Video video);

    //删除视频
    @Delete("DELETE FROM video WHERE video_id = #{videoId}")
    int removeVideoById(Long videoId);

    @Update("UPDATE video SET likes_count = likes_count + 1 WHERE video_id = #{videoId}")
    void incrementLikesCount(@Param("videoId") Long videoId);

    @Update("UPDATE video SET likes_count = CASE WHEN likes_count > 0 THEN likes_count - 1 ELSE 0 END WHERE video_id = #{videoId}")
    void decrementLikesCount(@Param("videoId") Long videoId);

    @Select("SELECT video_id, user_id, title, introduction, views_count, likes_count, create_time, surface_picture, video_address FROM video WHERE video_id = #{videoId}")
    Video findVideoById(@Param("videoId") Long videoId);

    @Select("<script>SELECT video_id, user_id, title, introduction, views_count, likes_count, create_time, surface_picture, video_address FROM video WHERE 1=1 <if test='year != null'> AND YEAR(create_time) = #{year}</if> <if test='category != null'> AND category = #{category}</if> <if test='startTime != null'> AND create_time &gt;= #{startTime}</if> <if test='endTime != null'> AND create_time &lt;= #{endTime}</if> <if test='keyword != null'> AND (title LIKE CONCAT('%',#{keyword},'%') OR introduction LIKE CONCAT('%',#{keyword},'%'))</if> <choose><when test='sortBy == \"views\"'> ORDER BY views_count ${order}</when><otherwise> ORDER BY create_time ${order}</otherwise></choose></script>")
    List<Video> searchVideos(Map<String, Object> params);

    @Update("UPDATE video SET views_count = views_count + 1 WHERE video_id = #{videoId}")
    void incrementViewsCount(Long videoId);

    //查询审核视频
    @Select("SELECT video_id,user_id,title,introduction,views_count,likes_count,create_time,surface_picture,video_address,status,reviewer_id,review_time FROM video WHERE status='PENDING' ORDER BY create_time DESC")
    List<Video> findPendingVideos();

    //更新审核状态
    @Update("UPDATE video SET status=#{status},reviewer_id=#{reviewerId},review_time=NOW() WHERE video_id=#{videoId}")
    void updateStatus(@Param("videoId") Long videoId, @Param("status") String status, @Param("reviewerId") Long reviewerId);
}
