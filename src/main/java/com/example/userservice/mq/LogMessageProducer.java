package com.example.userservice.mq;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志消息生产者
 * 负责向MQ发送操作日志消息
 */
@Component
@Slf4j
public class LogMessageProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 操作日志主题
     */
    private static final String LOG_TOPIC = "operation-log-topic";

    /**
     * 发送用户注册日志
     *
     * @param userId 用户ID
     * @param username 用户名
     */
    public void sendUserRegisterLog(Long userId, String username) {
        OperationLogMessage message = new OperationLogMessage();
        message.setUserId(userId);
        message.setUsername(username);
        message.setOperation("USER_REGISTER");
        message.setDescription("用户注册");
        message.setIpAddress(getCurrentIpAddress());
        message.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        sendLogMessage(message);
    }

    /**
     * 发送用户登录日志
     *
     * @param userId 用户ID
     * @param username 用户名
     */
    public void sendUserLoginLog(Long userId, String username) {
        OperationLogMessage message = new OperationLogMessage();
        message.setUserId(userId);
        message.setUsername(username);
        message.setOperation("USER_LOGIN");
        message.setDescription("用户登录");
        message.setIpAddress(getCurrentIpAddress());
        message.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        sendLogMessage(message);
    }

    /**
     * 发送用户信息更新日志
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param updateFields 更新字段
     */
    public void sendUserUpdateLog(Long userId, String username, String updateFields) {
        OperationLogMessage message = new OperationLogMessage();
        message.setUserId(userId);
        message.setUsername(username);
        message.setOperation("USER_UPDATE");
        message.setDescription("用户信息更新: " + updateFields);
        message.setIpAddress(getCurrentIpAddress());
        message.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        sendLogMessage(message);
    }

    /**
     * 发送密码重置日志
     *
     * @param userId 用户ID
     * @param username 用户名
     */
    public void sendPasswordResetLog(Long userId, String username) {
        OperationLogMessage message = new OperationLogMessage();
        message.setUserId(userId);
        message.setUsername(username);
        message.setOperation("PASSWORD_RESET");
        message.setDescription("密码重置");
        message.setIpAddress(getCurrentIpAddress());
        message.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        sendLogMessage(message);
    }

    /**
     * 发送用户查询日志
     *
     * @param operatorUserId 操作者用户ID
     * @param operatorUsername 操作者用户名
     * @param targetUserId 目标用户ID
     */
    public void sendUserQueryLog(Long operatorUserId, String operatorUsername, Long targetUserId) {
        OperationLogMessage message = new OperationLogMessage();
        message.setUserId(operatorUserId);
        message.setUsername(operatorUsername);
        message.setOperation("USER_QUERY");
        message.setDescription("查询用户信息: " + targetUserId);
        message.setIpAddress(getCurrentIpAddress());
        message.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        sendLogMessage(message);
    }

    /**
     * 发送日志消息到MQ
     *
     * @param message 日志消息
     */
    private void sendLogMessage(OperationLogMessage message) {
        try {
            rocketMQTemplate.convertAndSend(LOG_TOPIC, message);
            log.info("发送操作日志消息成功: {}", message);
        } catch (Exception e) {
            log.error("发送操作日志消息失败: {}", message, e);
        }
    }

    /**
     * 获取当前请求的IP地址（简化实现）
     * 实际项目中需要从HttpServletRequest中获取
     *
     * @return IP地址
     */
    private String getCurrentIpAddress() {
        // 简化实现，实际项目中需要注入HttpServletRequest
        return "127.0.0.1";
    }
}

/**
 * 操作日志消息DTO
 */
@Data
class OperationLogMessage {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 操作描述
     */
    private String description;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 操作时间
     */
    private String timestamp;

    /**
     * 额外数据（JSON格式）
     */
    private String extraData;
}