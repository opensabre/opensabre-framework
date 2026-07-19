package io.github.opensabre.governance.usage;

import io.github.opensabre.eda.api.EdaEvent;
import io.github.opensabre.eda.api.EdaEventPublisher;
import io.github.opensabre.eda.api.EventTarget;

import java.util.UUID;

/**
 * 通过 EDA 投递使用量记录的实现。
 */
public class EdaUsageCounterRecorder implements UsageCounterRecorder {

    private final EdaEventPublisher eventPublisher;

    public EdaUsageCounterRecorder(EdaEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void record(UsageRecord record) {
        String eventId = record.recordId() == null ? UUID.randomUUID().toString() : record.recordId();
        UsageRecord message = new UsageRecord(eventId, record.occurredAt(), record.source(), record.objectType(),
                record.objectId(), record.eventType(), record.outcome());
        String producer = message.source() == null || message.source().isBlank() ? "governance" : message.source();
        eventPublisher.publish(new EdaEvent<>(eventId, UsageEventTypes.EDA_EVENT_TYPE, message.occurredAt(),
                producer, null, null, message), EventTarget.REMOTE);
    }
}
