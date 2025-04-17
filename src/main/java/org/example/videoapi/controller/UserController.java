package org.example.videoapi.controller;


import org.example.videoapi.config.AliOssProperties;
import org.example.videoapi.pojo.dto.LoginRequest;
import org.example.videoapi.pojo.dto.RegisterRequest;
import org.example.videoapi.pojo.dto.ResultResponse;
import org.example.videoapi.pojo.entity.User;
import org.example.videoapi.service.UserService;
import org.example.videoapi.util.AliOssUtil;
import org.example.videoapi.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private AliOssProperties aliOssProperties;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/register")
    public ResultResponse<String> register(
            @RequestBody RegisterRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        // 获取注册用户的角色（普通用户/管理员）
        String role = request.getRole();
        // 如果是注册管理员账号，需要校验当前登录者是否为管理员
        if ("ADMIN".equalsIgnoreCase(role)) {
            if (token == null || token.isBlank()) {
                return ResultResponse.error(403, "创建管理员需要登录");
            }
            String rawToken = token.replace("Bearer ", "");
            String requesterRole = jwtTokenUtil.getRoleFromToken(rawToken);
            if (!"ADMIN".equalsIgnoreCase(requesterRole)) {
                return ResultResponse.error(403, "只有管理员可以创建管理员账号");
            }
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRole(role);
        userService.register(user);
        return ResultResponse.success("用户注册成功", null);
    }

    @PostMapping("/login")
    public ResultResponse<String> login(@RequestBody LoginRequest request) {
        String token = userService.login(request.getUsername(), request.getPassword());
        return ResultResponse.success("用户登陆成功", token);
    }


    @GetMapping("/{userId}")
    public ResultResponse<User> getUserInfo(@PathVariable Long userId) {
        return ResultResponse.success("成功查询到用户信息", userService.getUserInfo(userId));
    }


    @PostMapping("/{userId}/avatar")
    public ResultResponse<String> uploadAvatar(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        // 1. 上传到 OSS
        String avatarUrl = aliOssUtil.uploadFile(file, aliOssProperties.getAvatarDir());
        // 2. 更新数据库中的 avatar 字段
        userService.updateAvatar(userId, avatarUrl);
        // 3. 返回新 URL
        return ResultResponse.success("头像更新成功", avatarUrl);
    }
}
