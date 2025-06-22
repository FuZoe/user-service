package com.example.userservice.mq;

import lombok.Data;

/**
 * 操作日志消息DTO
 */
@Data
public class OperationLogMessage {

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