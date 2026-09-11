package io.github.opensabre.monitoring;

import io.github.opensabre.monitoring.model.MonitoringInstance;
import io.github.opensabre.security.actuator.ActuatorMonitoringTokenIssuer;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActuatorMetricsClientTest {

    @Test
    void buildsMetricUriFromStandardDiscoveryMetadata() {
        var instance = new MonitoringInstance("10.0.0.8", 8080, true,
                Map.of("management.scheme", "https", "management.host", "base-sysadmin",
                        "management.port", "9090", "management.path", "/manage"));

        var uri = new ActuatorMetricsClient(null, new ObjectMapper(),
                mock(ActuatorMonitoringTokenIssuer.class), new MonitoringProperties())
                .metricUri(instance, "jvm.memory.used", "area:heap");

        assertThat(uri.toString()).isEqualTo(
                "https://base-sysadmin:9090/manage/metrics/jvm.memory.used?tag=area%3Aheap");
    }

    @Test
    void retainsLegacyRuntimeSchemeMetadataCompatibility() {
        var instance = new MonitoringInstance("10.0.0.8", 8080, true,
                Map.of("runtime.scheme", "https"));

        var uri = new ActuatorMetricsClient(null, new ObjectMapper(),
                mock(ActuatorMonitoringTokenIssuer.class), new MonitoringProperties())
                .metricUri(instance, "process.uptime", null);

        assertThat(uri.toString()).isEqualTo(
                "https://10.0.0.8:8080/actuator/metrics/process.uptime");
    }

    @Test
    void signsEveryMetricRequestForTheTargetApplication() throws Exception {
        var tokenIssuer = mock(ActuatorMonitoringTokenIssuer.class);
        when(tokenIssuer.issue("iqc-platform")).thenReturn("signed-monitoring-token");
        var httpClient = mock(java.net.http.HttpClient.class);
        when(httpClient.send(any(java.net.http.HttpRequest.class),
                any(java.net.http.HttpResponse.BodyHandler.class)))
                .thenAnswer(invocation -> {
                    java.net.http.HttpRequest request = invocation.getArgument(0);
                    assertThat(request.headers().firstValue("x-client-token"))
                            .contains("signed-monitoring-token");
                    return response("{\"measurements\":[{\"statistic\":\"VALUE\",\"value\":1}]}");
                });

        var snapshot = new ActuatorMetricsClient(httpClient, new ObjectMapper(), tokenIssuer,
                new MonitoringProperties()).fetch("iqc-platform",
                new MonitoringInstance("10.0.0.8", 8080, true, Map.of()));

        assertThat(snapshot.processCpuUsage()).isEqualTo(1);
        verify(tokenIssuer).issue("iqc-platform");
    }

    @SuppressWarnings("unchecked")
    private static java.net.http.HttpResponse<String> response(String body) {
        var response = mock(java.net.http.HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(body);
        return response;
    }
}
