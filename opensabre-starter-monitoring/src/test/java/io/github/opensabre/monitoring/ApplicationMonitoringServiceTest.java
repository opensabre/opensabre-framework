package io.github.opensabre.monitoring;

import io.github.opensabre.monitoring.model.ApplicationRuntimeMetrics;
import io.github.opensabre.monitoring.model.MonitoringInstance;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationMonitoringServiceTest {

    @Test
    void degradesFailedInstancesWithoutDroppingHealthySnapshots() {
        ActuatorMetricsClient client = mock(ActuatorMetricsClient.class);
        var healthy = new MonitoringInstance("10.0.0.1", 8080, true, Map.of());
        var unavailable = new MonitoringInstance("10.0.0.2", 8080, false, Map.of());
        when(client.fetch("orders", healthy)).thenReturn(new ApplicationRuntimeMetrics(0.2, 10, 20, 30, 4));
        when(client.fetch("orders", unavailable)).thenThrow(new IllegalStateException("HTTP 503"));

        var results = new ApplicationMonitoringService(client)
                .snapshots("orders", List.of(healthy, unavailable));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).snapshot()).isNotNull();
        assertThat(results.get(0).errorMessage()).isNull();
        assertThat(results.get(1).snapshot()).isNull();
        assertThat(results.get(1).errorMessage()).isEqualTo("HTTP 503");
    }
}
