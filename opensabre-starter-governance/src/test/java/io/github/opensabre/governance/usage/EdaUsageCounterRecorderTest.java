package io.github.opensabre.governance.usage;

import io.github.opensabre.eda.api.EdaEvent;
import io.github.opensabre.eda.api.EdaEventPublisher;
import io.github.opensabre.eda.api.EventTarget;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EdaUsageCounterRecorderTest {

    @Test
    void shouldPublishUsageRecordToRemoteTransport() {
        EdaEventPublisher publisher = mock(EdaEventPublisher.class);
        EdaUsageCounterRecorder recorder = new EdaUsageCounterRecorder(publisher);
        UsageRecord record = new UsageRecord(null, Instant.parse("2026-07-19T00:00:00Z"), null,
                "CAPTCHA_SCENE", "login", "CAPTCHA_GENERATE", UsageOutcome.SUCCESS);
        ArgumentCaptor<EdaEvent> eventCaptor = ArgumentCaptor.forClass(EdaEvent.class);

        recorder.record(record);

        verify(publisher).publish(eventCaptor.capture(), eq(EventTarget.REMOTE));
        EdaEvent<?> event = eventCaptor.getValue();
        assertEquals(UsageEventTypes.EDA_EVENT_TYPE, event.eventType());
        UsageRecord payload = (UsageRecord) event.payload();
        assertNotNull(payload.recordId());
        assertEquals("governance", event.producer());
        assertEquals(UsageOutcome.SUCCESS, payload.outcome());
    }
}
