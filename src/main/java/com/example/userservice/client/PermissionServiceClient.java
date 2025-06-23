package com.example.userservice.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 权限服务RPC客户端
 * 通过Feign调用权限服务的接口
 */
@FeignClient(name = "permission-service", fallback = PermissionServiceClientFallback.class)
@ConditionalOnProperty(name = "spring.cloud.nacos.discovery.enabled", havingValue = "true")
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