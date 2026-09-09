package io.github.opensabre.security.actuator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActuatorMonitoringAccessTest {

    @Test
    void definesTheControlPlaneMetricContractOnce() {
        assertThat(ActuatorMonitoringAccess.metricPaths()).containsExactly(
                "/actuator/metrics/process.cpu.usage",
                "/actuator/metrics/jvm.memory.used",
                "/actuator/metrics/jvm.memory.max",
                "/actuator/metrics/process.uptime",
                "/actuator/metrics/jvm.threads.live");
        assertThat(ActuatorMonitoringAccess.AUTHORITY).isEqualTo("ACTUATOR_METRICS_READ");
    }
}
