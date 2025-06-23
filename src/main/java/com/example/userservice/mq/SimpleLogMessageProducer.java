package com.example.userservice.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 简化版日志消息生产者
 * 当RocketMQ未配置时使用，只记录日志不发送MQ
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "rocketmq.name-server", havingValue = "", matchIfMissing = true)
public class SimpleLogMessageProducer {

    /**
     * 发送用户注册日志
     *
     * @param userId 用户ID
     * @param username 用户名
     */
    public void sendUserRegisterLog(Long userId, String username) {
        log.info("用户注册日志: userId={}, username={}, operation=USER_REGISTER, description=用户注册", 
                userId, username);
    }

    /**
     * 发送用户登录日志
     *
     * @param userId 用户ID
     * @param username 用户名
     */
    public void sendUserLoginLog(Long userId, String username) {
        log.info("用户登录日志: userId={}, username={}, operation=USER_LOGIN, description=用户登录", 
                userId, username);
    }

    /**
     * 发送用户信息更新日志
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param updateFields 更新字段
     */
    public void sendUserUpdateLog(Long userId, String username, String updateFields) {
        log.info("用户更新日志: userId={}, username={}, operation=USER_UPDATE, description=用户信息更新: {}", 
                userId, username, updateFields);
    }

    /**
     * 发送密码重置日志
     *
     * @param userId 用户ID
     * @param username 用户名
     */
    public void sendPasswordResetLog(Long userId, String username) {
        log.info("密码重置日志: userId={}, username={}, operation=PASSWORD_RESET, description=密码重置", 
                userId, username);
    }

    /**
     * 发送用户查询日志
     *
     * @param operatorUserId 操作者用户ID
     * @param operatorUsername 操作者用户名
     * @param targetUserId 目标用户ID
     */
    public void sendUserQueryLog(Long operatorUserId, String operatorUsername, Long targetUserId) {
        log.info("用户查询日志: operatorUserId={}, operatorUsername={}, targetUserId={}, operation=USER_QUERY, description=查询用户信息", 
                operatorUserId, operatorUsername, targetUserId);
    }
} 