package io.github.opensabre.governance.usage;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UsageCounterRecorderTest {

    @Test
    void shouldCreateTypedSuccessRecord() {
        AtomicReference<UsageRecord> captured = new AtomicReference<>();
        UsageCounterRecorder recorder = captured::set;

        recorder.success("REPORT", "monthly", "EXPORT");

        UsageRecord record = captured.get();
        assertEquals("REPORT", record.objectType());
        assertEquals("monthly", record.objectId());
        assertEquals("EXPORT", record.eventType());
        assertEquals(UsageOutcome.SUCCESS, record.outcome());
        assertNotNull(record.occurredAt());
    }
}
