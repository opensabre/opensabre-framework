package io.github.opensabre.governance.usage;

import java.time.Instant;

/**
 * 通用异步计次入口。具体传输方式由 governance 自动配置选择。
 */
public interface UsageCounterRecorder {

    void record(UsageRecord record);

    default void attempt(String objectType, String objectId, String eventType) {
        record(new UsageRecord(null, Instant.now(), null, objectType, objectId, eventType, UsageOutcome.ATTEMPT));
    }

    default void success(String objectType, String objectId, String eventType) {
        record(new UsageRecord(null, Instant.now(), null, objectType, objectId, eventType, UsageOutcome.SUCCESS));
    }

    default void failure(String objectType, String objectId, String eventType) {
        record(new UsageRecord(null, Instant.now(), null, objectType, objectId, eventType, UsageOutcome.FAILURE));
    }
}
