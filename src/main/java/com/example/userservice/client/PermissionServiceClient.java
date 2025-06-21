package com.example.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import lombok.Data;

/**
 * 权限服务RPC客户端
 * 通过Feign调用权限服务的接口
 */
@FeignClient(name = "permission-service", fallback = PermissionServiceClientFallback.class)
public interface PermissionServiceClient {

    /**
     * 为用户绑定默认角色
     *
     * @param userId 用户ID
     * @return 绑定结果
     */
    @PostMapping("/permission/bind-default-role")
    BindRoleResponse bindDefaultRole(@RequestParam("userId") Long userId);

    /**
     * 获取用户权限信息
     *
     * @param userId 用户ID
     * @return 用户权限信息
     */
    @GetMapping("/permission/user/{userId}")
    UserPermissionResponse getUserPermission(@PathVariable("userId") Long userId);

    /**
     * 检查用户是否有指定权限
     *
     * @param userId 用户ID
     * @param permission 权限码
     * @return 权限检查结果
     */
    @GetMapping("/permission/check")
    PermissionCheckResponse checkPermission(@RequestParam("userId") Long userId,
                                            @RequestParam("permission") String permission);
}

/**
 * 角色绑定响应DTO
 */
@Data
class BindRoleResponse {
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

/**
 * 用户权限响应DTO
 */
@Data
class UserPermissionResponse {
    private Long userId;
    private String roleCode;
    private String roleName;
    private java.util.List<String> permissions;

    public UserPermissionResponse() {}

    public UserPermissionResponse(Long userId, String roleCode, String roleName, java.util.List<String> permissions) {
        this.userId = userId;
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.permissions = permissions;
    }
}

/**
 * 权限检查响应DTO
 */
@Data
class PermissionCheckResponse {
    private Boolean hasPermission;
    private String message;

    public PermissionCheckResponse() {}

    public PermissionCheckResponse(Boolean hasPermission, String message) {
        this.hasPermission = hasPermission;
        this.message = message;
    }
}

/**
 * 权限服务降级处理
 */
@org.springframework.stereotype.Component
class PermissionServiceClientFallback implements PermissionServiceClient {

    @Override
    public BindRoleResponse bindDefaultRole(Long userId) {
        return new BindRoleResponse(false, "权限服务暂不可用，角色绑定失败", null);
    }

    @Override
    public UserPermissionResponse getUserPermission(Long userId) {
        return new UserPermissionResponse(userId, "USER", "普通用户",
                java.util.Arrays.asList("USER:READ:SELF"));
    }

    @Override
    public PermissionCheckResponse checkPermission(Long userId, String permission) {
        return new PermissionCheckResponse(false, "权限服务暂不可用，权限检查失败");
    }
}