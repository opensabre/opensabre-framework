package io.github.opensabre.monitoring.model;

/** Per-instance monitoring result with explicit degradation when metrics are unavailable. */
public record ApplicationInstanceMonitoring(String serviceName, String instanceId, boolean healthy,
        ApplicationRuntimeMetrics snapshot, String errorMessage) {
}
