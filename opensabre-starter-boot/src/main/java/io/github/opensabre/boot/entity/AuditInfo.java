package io.github.opensabre.boot.entity;

import lombok.*;

import java.time.LocalDateTime;

import io.github.opensabre.boot.annotations.OperationType;

/**
 * 审计日志实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditInfo {

    /**
     * 操作类型
     */
    private OperationType operationType;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 操作人用户名
     */
    private String operatorUsername;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作IP地址
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数
     */
    private String request;

    /**
     * 操作结果
     */
    private String response;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行时间(毫秒)
     */
    private Long executionTime;

    /**
     * 操作目标key
     */
    private String targetKey;
}