package org.example.videoapi.service.impl;

import org.example.videoapi.pojo.entity.User;
import org.example.videoapi.exception.ApiException;
import org.example.videoapi.mapper.UserMapper;
import org.example.videoapi.service.UserService;
import org.example.videoapi.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Override
    public void register(User user) {
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new ApiException("用户名已经存在", 400);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
    }

    @Override
    public String login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiException("账号或密码错误", 401);
        }
        return jwtUtil.generateToken(user);
    }

    @Override
    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ApiException("未找到该用户", 404);
        }
        return user;
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ApiException("未找到该用户", 404);
        }
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
    }
}
