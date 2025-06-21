package com.example.userservice.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 用户注册请求DTO
 */
@Data
public class UserRegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}

/**
 * 用户登录请求DTO
 */
@Data
class UserLoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}

/**
 * 用户登录响应DTO
 */
@Data
class UserLoginResponse {

    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String token;
    private Long expiration;

    public UserLoginResponse(Long userId, String username, String email, String phone, String token, Long expiration) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.token = token;
        this.expiration = expiration;
    }
}

/**
 * 用户信息响应DTO
 */
@Data
class UserInfoResponse {

    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String gmtCreate;
    private String gmtModified;

    public UserInfoResponse(Long userId, String username, String email, String phone, String gmtCreate, String gmtModified) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.gmtCreate = gmtCreate;
        this.gmtModified = gmtModified;
    }
}

/**
 * 用户更新请求DTO
 */
@Data
class UserUpdateRequest {

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}

/**
 * 密码重置请求DTO
 */
@Data
class PasswordResetRequest {

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
    private String newPassword;
}

/**
 * 统一响应结果DTO
 */
@Data
class ApiResponse<T> {

    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "成功", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}

/**
 * 分页响应DTO
 */
@Data
class PageResponse<T> {

    private java.util.List<T> content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private Boolean first;
    private Boolean last;

    public PageResponse(org.springframework.data.domain.Page<T> page) {
        this.content = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.first = page.isFirst();
        this.last = page.isLast();
    }
}