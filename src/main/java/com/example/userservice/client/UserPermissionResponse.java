package com.example.userservice.client;

import lombok.Data;
import java.util.List;

/**
 * 用户权限响应DTO
 */
@Data
public class UserPermissionResponse {
    private Long userId;
    private String roleCode;
    private String roleName;
    private List<String> permissions;

    public UserPermissionResponse() {}

    public UserPermissionResponse(Long userId, String roleCode, String roleName, List<String> permissions) {
        this.userId = userId;
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.permissions = permissions;
    }
} 