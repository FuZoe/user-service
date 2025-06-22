package com.example.userservice.client;

import lombok.Data;

/**
 * 角色绑定响应DTO
 */
@Data
public class BindRoleResponse {
    private Boolean success;
    private String message;
    private String roleCode;

    public BindRoleResponse() {}

    public BindRoleResponse(Boolean success, String message, String roleCode) {
        this.success = success;
        this.message = message;
        this.roleCode = roleCode;
    }
} 