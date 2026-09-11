package io.github.opensabre.monitoring.model;

/** Safe common process and JVM values returned by the monitoring collector. */
public record ApplicationRuntimeMetrics(double processCpuUsage, long heapUsedBytes,
        long heapMaxBytes, long uptimeSeconds, int liveThreads) {
}
