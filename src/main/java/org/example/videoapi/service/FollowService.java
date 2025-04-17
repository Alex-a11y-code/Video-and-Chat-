package org.example.videoapi.service;

import java.util.List;

import org.example.videoapi.pojo.vo.UserVO;

public interface FollowService {

    void follow(Long userId, Long followUserId);

    void unfollow(Long userId, Long followUserId);

    List<UserVO> getFansList(Long userId, int page, int pageSize);

    List<UserVO> getFollowList(Long userId, int page, int pageSize);

    List<UserVO> getFriendList(Long userId, int page, int pageSize);
}
