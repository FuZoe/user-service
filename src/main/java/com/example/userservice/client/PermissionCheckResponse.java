package com.example.userservice.client;

import lombok.Data;

/**
 * 权限检查响应DTO
 */
@Data
public class PermissionCheckResponse {
    private Boolean hasPermission;
    private String message;

    public PermissionCheckResponse() {}

    public PermissionCheckResponse(Boolean hasPermission, String message) {
        this.hasPermission = hasPermission;
        this.message = message;
    }
} 