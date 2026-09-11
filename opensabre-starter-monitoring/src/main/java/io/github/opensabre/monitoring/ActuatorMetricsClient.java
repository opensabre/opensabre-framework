package io.github.opensabre.monitoring;

import io.github.opensabre.monitoring.model.ApplicationRuntimeMetrics;
import io.github.opensabre.monitoring.model.MonitoringInstance;
import io.github.opensabre.security.actuator.ActuatorMonitoringTokenIssuer;
import io.github.opensabre.security.token.InternalTokenConstants;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/** Reads the fixed, non-sensitive Actuator metric contract using a least-privilege internal token. */
public class ActuatorMetricsClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ActuatorMonitoringTokenIssuer tokenIssuer;
    private final MonitoringProperties properties;

    public ActuatorMetricsClient(ObjectMapper objectMapper, ActuatorMonitoringTokenIssuer tokenIssuer,
            MonitoringProperties properties) {
        this(HttpClient.newBuilder().connectTimeout(properties.getCollector().getConnectTimeout()).build(),
                objectMapper, tokenIssuer, properties);
    }

    ActuatorMetricsClient(HttpClient httpClient, ObjectMapper objectMapper,
            ActuatorMonitoringTokenIssuer tokenIssuer, MonitoringProperties properties) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.tokenIssuer = tokenIssuer;
        this.properties = properties;
    }

    /** Fetch CPU, heap, uptime and live-thread values from one discovered instance. */
    public ApplicationRuntimeMetrics fetch(String applicationName, MonitoringInstance instance) {
        String token = tokenIssuer.issue(applicationName);
        double cpu = metric(instance, "process.cpu.usage", null, token);
        long heapUsed = Math.round(metric(instance, "jvm.memory.used", "area:heap", token));
        long heapMax = Math.round(metric(instance, "jvm.memory.max", "area:heap", token));
        long uptime = Math.round(metric(instance, "process.uptime", null, token));
        int liveThreads = (int) Math.round(metric(instance, "jvm.threads.live", null, token));
        return new ApplicationRuntimeMetrics(cpu, heapUsed, heapMax, uptime, liveThreads);
    }

    private double metric(MonitoringInstance instance, String metricName, String tag, String token) {
        HttpRequest request = HttpRequest.newBuilder(metricUri(instance, metricName, tag))
                .header(InternalTokenConstants.HEADER, token)
                .timeout(properties.getCollector().getReadTimeout()).GET().build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Actuator metrics endpoint returned HTTP " + response.statusCode());
            }
            JsonNode measurements = objectMapper.readTree(response.body()).path("measurements");
            for (JsonNode measurement : measurements) {
                if ("VALUE".equals(measurement.path("statistic").asText())) {
                    return measurement.path("value").asDouble();
                }
            }
            throw new IllegalStateException("Actuator metric has no VALUE measurement: " + metricName);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Actuator metrics request was interrupted", exception);
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot read Actuator metric: " + metricName, exception);
        }
    }

    URI metricUri(MonitoringInstance instance, String metricName, String tag) {
        String scheme = instance.metadata().getOrDefault("management.scheme",
                instance.metadata().getOrDefault("runtime.scheme", "http"));
        String host = instance.metadata().getOrDefault("management.host", instance.host());
        String port = instance.metadata().getOrDefault("management.port", Integer.toString(instance.port()));
        String path = instance.metadata().getOrDefault("management.path", "/actuator");
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Actuator management path must start with /");
        }
        String uri = scheme + "://" + host + ":" + port + path + "/metrics/"
                + URLEncoder.encode(metricName, StandardCharsets.UTF_8);
        if (tag != null) {
            uri += "?tag=" + URLEncoder.encode(tag, StandardCharsets.UTF_8);
        }
        return URI.create(uri);
    }
}
