package org.example.videoapi.mapper;

import org.apache.ibatis.annotations.*;
import org.example.videoapi.pojo.entity.ChatGroup;
import org.example.videoapi.pojo.entity.GroupMember;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface GroupMapper {
    //创建群组
    @Insert("INSERT INTO chat_group(name, creator_id, description) VALUES(#{name}, #{creatorId}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")

    int insertGroup(ChatGroup group);
    //添加成员
    @Insert("INSERT INTO group_member(group_id, user_id) VALUES(#{groupId}, #{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertMember(GroupMember member);

    //删除成员
    @Delete("DELETE FROM group_member WHERE group_id=#{groupId} AND user_id=#{userId}")
    int removeMember(@Param("groupId") Long groupId, @Param("userId") Long userId);

    //查找群组及相关部分
    @Select("SELECT * FROM chat_group WHERE id=#{id}")
    ChatGroup findGroupById(@Param("id") Long id);

    @Select("SELECT g.* FROM chat_group g JOIN group_member m ON g.id = m.group_id WHERE m.user_id=#{userId}")
    List<ChatGroup> findGroupsByUserId(@Param("userId") Long userId);

    @Select("SELECT user_id FROM group_member WHERE group_id=#{groupId}")
    List<Long> findGroupMemberIds(@Param("groupId") Long groupId);
}