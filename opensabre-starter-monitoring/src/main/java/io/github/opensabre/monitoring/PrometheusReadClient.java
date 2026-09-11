package io.github.opensabre.monitoring;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/** Executes read-only instant Prometheus queries without persisting monitoring data. */
public class PrometheusReadClient {

    private final HttpClient httpClient;
    private final MonitoringProperties properties;

    public PrometheusReadClient(MonitoringProperties properties) {
        this(properties, HttpClient.newBuilder()
                .connectTimeout(properties.getCollector().getConnectTimeout()).build());
    }

    PrometheusReadClient(MonitoringProperties properties, HttpClient httpClient) {
        this.properties = properties;
        this.httpClient = httpClient;
    }

    /** Execute one controlled PromQL instant query and return the raw response JSON. */
    public String query(String promql) {
        if (promql == null || promql.isBlank()) {
            throw new IllegalArgumentException("PromQL must not be blank");
        }
        HttpRequest request = HttpRequest.newBuilder(queryUri(promql))
                .timeout(properties.getCollector().getReadTimeout()).GET().build();
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Prometheus query returned HTTP " + response.statusCode());
            }
            return response.body();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot query Prometheus", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Prometheus query was interrupted", exception);
        }
    }

    URI queryUri(String promql) {
        String baseUrl = properties.getPrometheus().getServerUrl().replaceAll("/$", "");
        return URI.create(baseUrl + "/api/v1/query?query="
                + URLEncoder.encode(promql, StandardCharsets.UTF_8));
    }
}
