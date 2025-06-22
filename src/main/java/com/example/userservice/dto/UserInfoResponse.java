package com.example.userservice.dto;

import lombok.Data;

/**
 * 用户信息响应DTO
 */
@Data
public class UserInfoResponse {

    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String gmtCreate;
    private String gmtModified;

    public UserInfoResponse() {}

    public UserInfoResponse(Long userId, String username, String email, String phone, String gmtCreate, String gmtModified) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.gmtCreate = gmtCreate;
        this.gmtModified = gmtModified;
    }
} 