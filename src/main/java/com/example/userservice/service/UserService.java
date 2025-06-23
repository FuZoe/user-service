package com.example.userservice.service;

import com.example.userservice.client.PermissionServiceClient;
import com.example.userservice.client.BindRoleResponse;
import com.example.userservice.client.UserPermissionResponse;
import com.example.userservice.dto.*;
import com.example.userservice.entity.User;
import com.example.userservice.mq.LogMessageProducer;
import com.example.userservice.mq.SimpleLogMessageProducer;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.util.JwtUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * 用户业务服务层
 * 处理用户相关的业务逻辑
 */
@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private PermissionServiceClient permissionServiceClient;

    @Autowired(required = false)
    private LogMessageProducer logMessageProducer;

    @Autowired(required = false)
    private SimpleLogMessageProducer simpleLogMessageProducer;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private Environment environment;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册的统一入口
     * 根据当前激活的profile（环境）选择不同的注册流程
     */
    @Transactional
    public ApiResponse<String> register(UserRegisterRequest request) {
        // 1. 统一进行参数校验
        if (userRepository.existsByUsernameAndIsDeleted(request.getUsername(), 0)) {
            return ApiResponse.error(400, "用户名已存在");
        }
        if (request.getEmail() != null && userRepository.existsByEmailAndIsDeleted(request.getEmail(), 0)) {
            return ApiResponse.error(400, "邮箱已被注册");
        }
        if (request.getPhone() != null && userRepository.existsByPhoneAndIsDeleted(request.getPhone(), 0)) {
            return ApiResponse.error(400, "手机号已被注册");
        }

        // 2. 根据profile选择注册逻辑
        boolean isSimpleProfile = Arrays.asList(environment.getActiveProfiles()).contains("simple");
        if (isSimpleProfile) {
            return registerSimple(request);
        } else {
            return registerWithGlobalTransaction(request);
        }
    }

    /**
     * 带有分布式事务的注册流程（生产环境）
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    // 此处无需@Transactional，会由调用方register()的事务传播
    private ApiResponse<String> registerWithGlobalTransaction(UserRegisterRequest request) {
        // 创建用户
        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail(),
                request.getPhone()
        );
        User savedUser = userRepository.save(user);
        log.info("用户创建成功: userId={}, username={}", savedUser.getUserId(), savedUser.getUsername());

        // RPC调用权限服务绑定默认角色
        BindRoleResponse roleResponse = permissionServiceClient.bindDefaultRole(savedUser.getUserId());
        if (roleResponse == null || !roleResponse.getSuccess()) {
            throw new RuntimeException("RPC调用失败: 角色绑定失败" + (roleResponse != null ? " - " + roleResponse.getMessage() : ""));
        }
        log.info("角色绑定成功: userId={}, roleCode={}", savedUser.getUserId(), roleResponse.getRoleCode());

        // 异步发送注册日志到MQ
        logMessageProducer.sendUserRegisterLog(savedUser.getUserId(), savedUser.getUsername());

        return ApiResponse.success("注册成功");
    }

    /**
     * 简化的注册流程（simple模式）
     */
    // 此处无需@Transactional，会由调用方register()的事务传播
    private ApiResponse<String> registerSimple(UserRegisterRequest request) {
        // 创建用户
        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail(),
                request.getPhone()
        );
        User savedUser = userRepository.save(user);
        log.info("用户创建成功 (simple mode): userId={}, username={}", savedUser.getUserId(), savedUser.getUsername());

        // 异步发送日志（简化版）
        if (simpleLogMessageProducer != null) {
            simpleLogMessageProducer.sendUserRegisterLog(savedUser.getUserId(), savedUser.getUsername());
        }

        return ApiResponse.success("注册成功");
    }

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录结果
     */
    public ApiResponse<UserLoginResponse> login(UserLoginRequest request) {
        try {
            // 1. 查找用户
            Optional<User> userOpt = userRepository.findByUsernameAndIsDeleted(request.getUsername(), 0);
            if (!userOpt.isPresent()) {
                return ApiResponse.error(401, "用户名或密码错误");
            }

            User user = userOpt.get();

            // 2. 验证密码
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ApiResponse.error(401, "用户名或密码错误");
            }

            // 3. 生成JWT Token
            String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());
            Long expiration = System.currentTimeMillis() + 86400000; // 24小时

            // 4. 构造响应
            UserLoginResponse response = new UserLoginResponse(
                    user.getUserId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPhone(),
                    token,
                    expiration
            );

            // 5. 异步发送登录日志
            boolean isSimpleProfile = Arrays.asList(environment.getActiveProfiles()).contains("simple");
            if (isSimpleProfile) {
                if (simpleLogMessageProducer != null) {
                    simpleLogMessageProducer.sendUserLoginLog(user.getUserId(), user.getUsername());
                }
            } else {
                if (logMessageProducer != null) {
                    logMessageProducer.sendUserLoginLog(user.getUserId(), user.getUsername());
                }
            }

            return ApiResponse.success("登录成功", response);

        } catch (Exception e) {
            log.error("用户登录失败", e);
            return ApiResponse.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询用户列表（根据权限返回不同数据）
     *
     * @param page 页码
     * @param size 每页大小
     * @param currentUserId 当前用户ID
     * @return 用户列表
     */
    public ApiResponse<PageResponse<UserInfoResponse>> getUsers(Integer page, Integer size, Long currentUserId) {
        try {
            boolean isSimpleProfile = Arrays.asList(environment.getActiveProfiles()).contains("simple");

            // 1. 获取当前用户权限
            UserPermissionResponse permission;
            if (isSimpleProfile || permissionServiceClient == null) {
                 log.warn("权限服务未配置，使用默认USER权限");
                 permission = new UserPermissionResponse(currentUserId, "USER", "普通用户", Collections.singletonList("READ_SELF"));
            } else {
                try {
                    permission = permissionServiceClient.getUserPermission(currentUserId);
                } catch (Exception e) {
                    log.error("获取用户权限失败，将使用默认USER权限", e);
                    permission = new UserPermissionResponse(currentUserId, "USER", "普通用户", Collections.singletonList("READ_SELF"));
                }
            }

            // 2. 构建分页参数
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "gmtCreate"));
            Page<User> userPage;

            // 3. 根据角色权限查询不同范围的数据
            switch (permission.getRoleCode()) {
                case "SUPER_ADMIN":
                case "ADMIN":
                    userPage = userRepository.findAllActiveUsers(pageable);
                    break;
                case "USER":
                default:
                    Optional<User> currentUser = userRepository.findActiveUserById(currentUserId);
                    userPage = currentUser.map(user -> (Page<User>) new org.springframework.data.domain.PageImpl<>(Collections.singletonList(user), pageable, 1))
                                          .orElseGet(() -> Page.empty(pageable));
                    break;
            }

            // 4. 转换为响应DTO
            Page<UserInfoResponse> responsePage = userPage.map(this::convertToUserInfoResponse);

            // 5. 发送查询日志
            if (!isSimpleProfile && logMessageProducer != null) {
                logMessageProducer.sendUserQueryLog(currentUserId, permission.getRoleName(), null);
            }

            return ApiResponse.success(new PageResponse<>(responsePage));

        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询用户信息
     *
     * @param userId 用户ID
     * @param currentUserId 当前用户ID
     * @return 用户信息
     */
    public ApiResponse<UserInfoResponse> getUserById(Long userId, Long currentUserId) {
        try {
            boolean isSimpleProfile = Arrays.asList(environment.getActiveProfiles()).contains("simple");
            UserPermissionResponse permission;

            if (isSimpleProfile || permissionServiceClient == null) {
                 permission = new UserPermissionResponse(currentUserId, "USER", "普通用户", Collections.singletonList("READ_SELF"));
            } else {
                try {
                    permission = permissionServiceClient.getUserPermission(currentUserId);
                } catch (Exception e) {
                     log.error("获取用户权限失败，将使用默认USER权限", e);
                    permission = new UserPermissionResponse(currentUserId, "USER", "普通用户", Collections.singletonList("READ_SELF"));
                }
            }

            // 2. 权限检查
            boolean canAccess = false;
            switch (permission.getRoleCode()) {
                case "SUPER_ADMIN":
                case "ADMIN":
                    canAccess = true;
                    break;
                case "USER":
                default:
                    canAccess = userId.equals(currentUserId);
                    break;
            }

            if (!canAccess) {
                return ApiResponse.error(403, "无权限访问该用户信息");
            }

            // 3. 查询用户
            Optional<User> userOpt = userRepository.findActiveUserById(userId);
            if (!userOpt.isPresent()) {
                return ApiResponse.error(404, "用户不存在");
            }

            // 4. 转换响应
            UserInfoResponse response = convertToUserInfoResponse(userOpt.get());

            // 5. 发送查询日志
            if (!isSimpleProfile && logMessageProducer != null) {
                 logMessageProducer.sendUserQueryLog(currentUserId, permission.getRoleName(), userId);
            }
            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("查询用户信息失败: userId={}", userId, e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param request 更新请求
     * @param currentUserId 当前用户ID
     * @return 更新结果
     */
    @Transactional
    public ApiResponse<String> updateUser(Long userId, UserUpdateRequest request, Long currentUserId) {
        try {
            boolean isSimpleProfile = Arrays.asList(environment.getActiveProfiles()).contains("simple");
            UserPermissionResponse permission;
            if (isSimpleProfile || permissionServiceClient == null) {
                 permission = new UserPermissionResponse(currentUserId, "USER", "普通用户", Collections.singletonList("UPDATE_SELF"));
            } else {
                 try {
                    permission = permissionServiceClient.getUserPermission(currentUserId);
                 } catch (Exception e) {
                    log.error("获取用户权限失败，将使用默认USER权限", e);
                    permission = new UserPermissionResponse(currentUserId, "USER", "普通用户", Collections.singletonList("UPDATE_SELF"));
                 }
            }

            // 1. 权限检查（与查询逻辑类似）
            boolean canUpdate = false;

            switch (permission.getRoleCode()) {
                case "SUPER_ADMIN":
                case "ADMIN":
                    canUpdate = true;
                    break;
                case "USER":
                default:
                    canUpdate = userId.equals(currentUserId);
                    break;
            }

            if (!canUpdate) {
                return ApiResponse.error(403, "无权限修改该用户信息");
            }

            // 2. 查询用户
            Optional<User> userOpt = userRepository.findActiveUserById(userId);
            if (!userOpt.isPresent()) {
                return ApiResponse.error(404, "用户不存在");
            }

            User user = userOpt.get();

            // 3. 更新字段
            StringBuilder updateFields = new StringBuilder();
            if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmailAndIsDeleted(request.getEmail(), 0)) {
                    return ApiResponse.error(400, "邮箱已被使用");
                }
                user.setEmail(request.getEmail());
                updateFields.append("email,");
            }

            if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
                if (userRepository.existsByPhoneAndIsDeleted(request.getPhone(), 0)) {
                    return ApiResponse.error(400, "手机号已被使用");
                }
                user.setPhone(request.getPhone());
                updateFields.append("phone,");
            }

            // 4. 保存更新
            if (updateFields.length() > 0) {
                 userRepository.save(user);
                 // 5. 发送更新日志
                String fields = updateFields.substring(0, updateFields.length() - 1);
                if (isSimpleProfile) {
                    if (simpleLogMessageProducer != null) {
                        simpleLogMessageProducer.sendUserUpdateLog(userId, user.getUsername(), fields);
                    }
                } else {
                    if (logMessageProducer != null) {
                        logMessageProducer.sendUserUpdateLog(userId, user.getUsername(), fields);
                    }
                }
            }

            return ApiResponse.success("更新成功");

        } catch (Exception e) {
            log.error("更新用户信息失败: userId={}", userId, e);
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 重置密码
     *
     * @param request 重置请求
     * @param currentUserId 当前用户ID
     * @return 重置结果
     */
    @Transactional
    public ApiResponse<String> resetPassword(PasswordResetRequest request, Long currentUserId) {
        try {
            // 1. 查询当前用户
            Optional<User> userOpt = userRepository.findActiveUserById(currentUserId);
            if (!userOpt.isPresent()) {
                return ApiResponse.error(404, "用户不存在");
            }

            User user = userOpt.get();

            // 2. 验证旧密码
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                return ApiResponse.error(400, "旧密码不正确");
            }

            // 3. 更新密码
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // 4. 发送密码重置日志
            boolean isSimpleProfile = Arrays.asList(environment.getActiveProfiles()).contains("simple");
            if (isSimpleProfile) {
                if (simpleLogMessageProducer != null) {
                    simpleLogMessageProducer.sendPasswordResetLog(user.getUserId(), user.getUsername());
                }
            } else {
                 if (logMessageProducer != null) {
                    logMessageProducer.sendPasswordResetLog(user.getUserId(), user.getUsername());
                }
            }

            return ApiResponse.success("密码重置成功");

        } catch (Exception e) {
            log.error("密码重置失败: userId={}", currentUserId, e);
            return ApiResponse.error("密码重置失败: " + e.getMessage());
        }
    }

    /**
     * 将User实体转换为UserInfoResponse
     *
     * @param user 用户实体
     * @return 用户信息响应
     */
    private UserInfoResponse convertToUserInfoResponse(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new UserInfoResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getGmtCreate() != null ? user.getGmtCreate().format(formatter) : null,
                user.getGmtModified() != null ? user.getGmtModified().format(formatter) : null
        );
    }
}