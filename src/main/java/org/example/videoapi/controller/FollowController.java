package org.example.videoapi.controller;

import org.example.videoapi.pojo.dto.ResultResponse;
import org.example.videoapi.pojo.vo.UserVO;
import org.example.videoapi.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    /**
     * 关注、取消关注
     */

    @PostMapping("/follow")
    public ResultResponse<Void> follow(@RequestParam Long userId, @RequestParam Long followUserId) {
        followService.follow(userId, followUserId);
        return ResultResponse.success("关注成功", null);
    }

    @PostMapping("/unfollow")
    public ResultResponse<Void> unfollow(@RequestParam Long userId, @RequestParam Long followUserId) {
        followService.unfollow(userId, followUserId);
        return ResultResponse.success("取消关注成功", null);
    }

    /**
     * 获取粉丝列表
     */
    @GetMapping("/fans")
    public ResultResponse<List<UserVO>> getFansList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<UserVO> list = followService.getFansList(userId, page, pageSize);
        return ResultResponse.success("获取粉丝列表成功", list);
    }

    /**
     * 获取关注列表
     */
    @GetMapping("/follows")
    public ResultResponse<List<UserVO>> getFollowList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<UserVO> list = followService.getFollowList(userId, page, pageSize);
        return ResultResponse.success("获取关注列表成功", list);
    }

    /**
     * 获取好友列表（互相关注）
     */
    @GetMapping("/friends")
    public ResultResponse<List<UserVO>> getFriendList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<UserVO> list = followService.getFriendList(userId, page, pageSize);
        return ResultResponse.success("获取好友列表成功", list);
    }
}
