package io.github.opensabre.governance.usage;

import io.github.opensabre.governance.client.SysadminGovernanceClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class HttpUsageCounterRecorderTest {

    @Test
    void shouldNotAffectBusinessWhenSysadminIsUnavailable() {
        SysadminGovernanceClient client = mock(SysadminGovernanceClient.class);
        doThrow(new IllegalStateException("sysadmin unavailable")).when(client).recordUsage(org.mockito.ArgumentMatchers.any());
        HttpUsageCounterRecorder recorder = new HttpUsageCounterRecorder(client);

        assertDoesNotThrow(() -> recorder.success("REPORT", "monthly", "EXPORT"));
    }
}
