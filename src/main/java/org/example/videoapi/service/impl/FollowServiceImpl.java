package org.example.videoapi.service.impl;

import org.example.videoapi.exception.ApiException;
import org.example.videoapi.mapper.FollowMapper;
import org.example.videoapi.pojo.entity.Follow;
import org.example.videoapi.pojo.vo.UserVO;
import org.example.videoapi.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private FollowMapper followMapper;

    @Override
    public void follow(Long userId, Long followUserId) {
        if (userId.equals(followUserId)) {
            throw new ApiException("不能关注自己", 400);
        }
        int count = followMapper.getByUserAndFollow(userId, followUserId);
        if (count > 0) {
            throw new ApiException("已经关注该用户", 400);
        }


        Follow follow = new Follow();
        follow.setUserId(userId);
        follow.setFollowUserId(followUserId);

        followMapper.saveFollow(follow);
    }

    @Override
    public void unfollow(Long userId, Long followUserId) {
        int count = followMapper.getByUserAndFollow(userId, followUserId);
        if (count == 0) {
            throw new ApiException("未关注该用户，无法取消", 400);
        }
        followMapper.removeFollow(userId, followUserId);
    }

    @Override
    public List<UserVO> getFansList(Long userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return followMapper.getFansList(userId, offset, pageSize);
    }

    @Override
    public List<UserVO> getFollowList(Long userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return followMapper.getFollowList(userId, offset, pageSize);
    }

    @Override
    public List<UserVO> getFriendList(Long userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return followMapper.getFriendList(userId, offset, pageSize);
    }
}
