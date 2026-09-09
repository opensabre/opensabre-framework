package io.github.opensabre.security.actuator;

import java.util.List;

/** Shared access contract for the Actuator metrics consumed by the OpenSabre control plane. */
public final class ActuatorMonitoringAccess {

    /** Authority required on an internal token before basic Actuator metrics may be read. */
    public static final String AUTHORITY = "ACTUATOR_METRICS_READ";

    private static final List<String> METRIC_PATHS = List.of(
            "/actuator/metrics/process.cpu.usage",
            "/actuator/metrics/jvm.memory.used",
            "/actuator/metrics/jvm.memory.max",
            "/actuator/metrics/process.uptime",
            "/actuator/metrics/jvm.threads.live");

    private ActuatorMonitoringAccess() {
    }

    /** Returns the immutable endpoint paths read by service management. */
    public static List<String> metricPaths() {
        return METRIC_PATHS;
    }

    /** Returns the endpoint paths as an array for Spring Security matcher APIs. */
    public static String[] metricPathArray() {
        return METRIC_PATHS.toArray(String[]::new);
    }
}
