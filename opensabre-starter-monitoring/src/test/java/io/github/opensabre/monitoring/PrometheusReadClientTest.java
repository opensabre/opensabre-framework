package io.github.opensabre.monitoring;

import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrometheusReadClientTest {

    @Test
    void buildsEncodedInstantQueryUri() {
        MonitoringProperties properties = new MonitoringProperties();
        properties.getPrometheus().setServerUrl("http://prometheus:9090/");
        PrometheusReadClient client = new PrometheusReadClient(properties, HttpClient.newHttpClient());

        assertThat(client.queryUri("sum(rate(http_server_requests_seconds_count[5m]))").toString())
                .contains("/api/v1/query?query=sum%28rate%28http_server_requests_seconds_count%5B5m%5D%29%29");
        assertThatThrownBy(() -> client.query(" ")).isInstanceOf(IllegalArgumentException.class);
    }
}
