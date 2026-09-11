package io.github.opensabre.monitoring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Configuration for the optional control-plane metrics collector. */
@ConfigurationProperties("opensabre.monitoring")
public class MonitoringProperties {

    private final Collector collector = new Collector();
    private final Prometheus prometheus = new Prometheus();

    public Collector getCollector() {
        return collector;
    }

    public Prometheus getPrometheus() {
        return prometheus;
    }

    /** Outbound collector settings; disabled for ordinary applications. */
    public static class Collector {
        private boolean enabled;
        private Duration connectTimeout = Duration.ofSeconds(2);
        private Duration readTimeout = Duration.ofSeconds(3);

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public Duration getConnectTimeout() { return connectTimeout; }
        public void setConnectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; }
        public Duration getReadTimeout() { return readTimeout; }
        public void setReadTimeout(Duration readTimeout) { this.readTimeout = readTimeout; }
    }

    /** Read-only Prometheus query endpoint used by a control plane; results are never persisted. */
    public static class Prometheus {
        private String serverUrl = "http://localhost:9090";

        public String getServerUrl() { return serverUrl; }
        public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }
    }
}
