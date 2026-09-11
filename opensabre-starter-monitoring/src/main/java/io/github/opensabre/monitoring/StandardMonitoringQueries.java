package io.github.opensabre.monitoring;

/** Stable PromQL metric contract shared by OpenSabre monitoring control planes. */
public final class StandardMonitoringQueries {

    public static final String GATEWAY_REQUEST_RATE =
            "sum by (routeId) (rate(spring_cloud_gateway_requests_seconds_count[5m]))";
    public static final String GATEWAY_ERROR_RATE =
            "sum by (routeId) (rate(spring_cloud_gateway_requests_seconds_count{status=~\"5..\"}[5m]))";
    public static final String GATEWAY_P95_LATENCY =
            "histogram_quantile(0.95, sum by (routeId, le) "
                    + "(rate(spring_cloud_gateway_requests_seconds_bucket[5m])))";
    public static final String APPLICATION_REQUEST_RATE =
            "sum by (instance) (rate(http_server_requests_seconds_count[5m]))";
    public static final String APPLICATION_ERROR_RATE =
            "sum by (instance) (rate(http_server_requests_seconds_count{status=~\"5..\"}[5m]))";
    public static final String APPLICATION_P95_LATENCY =
            "histogram_quantile(0.95, sum by (instance, le) "
                    + "(rate(http_server_requests_seconds_bucket[5m])))";
    public static final String APPLICATION_CPU_USAGE = "max by (instance) (process_cpu_usage)";
    public static final String APPLICATION_HEAP_USED =
            "sum by (instance) (jvm_memory_used_bytes{area=\"heap\"})";
    public static final String APPLICATION_HEAP_MAX =
            "sum by (instance) (jvm_memory_max_bytes{area=\"heap\"})";

    private StandardMonitoringQueries() {
    }
}
