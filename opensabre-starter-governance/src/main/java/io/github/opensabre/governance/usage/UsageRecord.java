package io.github.opensabre.governance.usage;

import java.time.Instant;

/**
 * 跨模块、跨服务通用的对象使用计次记录。
 * <p>字段不得包含验证码内容、通知正文、手机号等敏感业务数据。</p>
 */
public record UsageRecord(String recordId, Instant occurredAt, String source, String objectType,
                          String objectId, String eventType, UsageOutcome outcome) {

    public UsageRecord {
        if (isBlank(objectType) || isBlank(objectId) || isBlank(eventType) || outcome == null) {
            throw new IllegalArgumentException("objectType, objectId, eventType and outcome are required");
        }
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
