package io.github.opensabre.monitoring.model;

import java.util.Map;

/** Framework-neutral discovered service instance used by the monitoring collector. */
public record MonitoringInstance(String host, int port, boolean healthy, Map<String, String> metadata) {

    public MonitoringInstance {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    /** Stable identifier shown by control-plane clients. */
    public String instanceId() {
        return host + ":" + port;
    }
}
