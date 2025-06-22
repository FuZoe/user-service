package com.example.userservice.dto;

import lombok.Data;

/**
 * 用户登录响应DTO
 */
@Data
public class UserLoginResponse {

    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String token;
    private Long expiration;

    public UserLoginResponse() {}

    public UserLoginResponse(Long userId, String username, String email, String phone, String token, Long expiration) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.token = token;
        this.expiration = expiration;
    }
} 