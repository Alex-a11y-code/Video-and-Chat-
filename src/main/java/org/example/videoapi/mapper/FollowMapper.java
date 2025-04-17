package org.example.videoapi.mapper;

import org.apache.ibatis.annotations.*;
import org.example.videoapi.pojo.entity.Follow;
import org.example.videoapi.pojo.vo.UserVO;

import java.util.List;

@Mapper
public interface FollowMapper {
    // 关注
    @Insert("INSERT INTO tb_follow (user_id, follow_user_id, create_time) VALUES (#{userId}, #{followUserId}, NOW())")
    void saveFollow(Follow follow);

    // 取消关注
    @Delete("DELETE FROM tb_follow WHERE user_id = #{userId} AND follow_user_id = #{followUserId}")
    void removeFollow(@Param("userId") Long userId, @Param("followUserId") Long followUserId);

    // 查询是否已关注
    @Select("SELECT COUNT(*) FROM tb_follow WHERE user_id = #{userId} AND follow_user_id = #{followUserId}")
    int getByUserAndFollow(@Param("userId") Long userId, @Param("followUserId") Long followUserId);

    // 获取粉丝列表
    @Select("SELECT u.id, u.username, u.avatar FROM user u JOIN tb_follow f ON u.id = f.user_id WHERE f.follow_user_id = #{userId} LIMIT #{offset}, #{pageSize}")
    List<UserVO> getFansList(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    // 获取关注列表
    @Select("SELECT u.id, u.username, u.avatar FROM user u JOIN tb_follow f ON u.id = f.follow_user_id WHERE f.user_id = #{userId} LIMIT #{offset}, #{pageSize}")
    List<UserVO> getFollowList(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    // 获取好友列表
    @Select("SELECT u.id, u.username, u.avatar FROM user u WHERE u.id IN (SELECT f1.follow_user_id FROM tb_follow f1 JOIN tb_follow f2 ON f1.follow_user_id = f2.user_id WHERE f1.user_id = #{userId} AND f2.follow_user_id = #{userId}) LIMIT #{offset}, #{pageSize}")
    List<UserVO> getFriendList(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);

}
