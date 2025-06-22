package com.example.userservice.client;

import org.springframework.stereotype.Component;
import java.util.Arrays;

/**
 * 权限服务降级处理
 */
@Component
public class PermissionServiceClientFallback implements PermissionServiceClient {

    @Override
    public BindRoleResponse bindDefaultRole(Long userId) {
        return new BindRoleResponse(false, "权限服务暂不可用，角色绑定失败", null);
    }

    @Override
    public UserPermissionResponse getUserPermission(Long userId) {
        return new UserPermissionResponse(userId, "USER", "普通用户",
                Arrays.asList("USER:READ:SELF"));
    }

    @Override
    public PermissionCheckResponse checkPermission(Long userId, String permission) {
        return new PermissionCheckResponse(false, "权限服务暂不可用，权限检查失败");
    }
} 