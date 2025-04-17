package org.example.videoapi.service;
import org.example.videoapi.pojo.entity.User;
/*
用户服务接口
 */
public interface UserService {
    void register(User user);
    String login(String username, String password);
    User getUserInfo(Long userId);
    void updateAvatar(Long userId, String avatarUrl);
}