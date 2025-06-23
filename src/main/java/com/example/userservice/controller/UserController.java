package com.example.userservice.controller;

import com.example.userservice.dto.*;
import com.example.userservice.service.UserService;
import com.example.userservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 提供用户相关的REST API接口
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("用户注册请求: username={}", request.getUsername());
        return userService.register(request);
    }

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public ApiResponse<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("用户登录请求: username={}", request.getUsername());
        return userService.login(request);
    }

    /**
     * 分页查询用户列表
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param request HTTP请求对象
     * @return 用户列表
     */
    @GetMapping("/users")
    public ApiResponse<PageResponse<UserInfoResponse>> getUsers(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ApiResponse.error(401, "未登录或token无效");
        }

        log.info("查询用户列表: page={}, size={}, currentUserId={}", page, size, currentUserId);
        return userService.getUsers(page, size, currentUserId);
    }

    /**
     * 根据ID查询用户信息
     *
     * @param userId 用户ID
     * @param request HTTP请求对象
     * @return 用户信息
     */
    @GetMapping("/info/{userId}")
    public ApiResponse<UserInfoResponse> getUserById(
            @PathVariable Long userId,
            HttpServletRequest request) {

        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ApiResponse.error(401, "未登录或token无效");
        }

        log.info("查询用户信息: userId={}, currentUserId={}", userId, currentUserId);
        return userService.getUserById(userId, currentUserId);
    }

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param updateRequest 更新请求
     * @param request HTTP请求对象
     * @return 更新结果
     */
    @PutMapping("/{userId}")
    public ApiResponse<String> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest updateRequest,
            HttpServletRequest request) {

        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ApiResponse.error(401, "未登录或token无效");
        }

        log.info("更新用户信息: userId={}, currentUserId={}", userId, currentUserId);
        return userService.updateUser(userId, updateRequest, currentUserId);
    }

    /**
     * 密码重置
     *
     * @param resetRequest 重置请求
     * @param request HTTP请求对象
     * @return 重置结果
     */
    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(
            @Valid @RequestBody PasswordResetRequest resetRequest,
            HttpServletRequest request) {

        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ApiResponse.error(401, "未登录或token无效");
        }

        log.info("密码重置请求: currentUserId={}", currentUserId);
        return userService.resetPassword(resetRequest, currentUserId);
    }

    /**
     * 从请求中获取当前用户ID
     *
     * @param request HTTP请求对象
     * @return 用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj != null) {
            return (Long) userIdObj;
        }
        return null;
    }
}