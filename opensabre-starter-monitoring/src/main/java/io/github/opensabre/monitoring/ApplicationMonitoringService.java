package io.github.opensabre.monitoring;

import io.github.opensabre.monitoring.model.ApplicationInstanceMonitoring;
import io.github.opensabre.monitoring.model.MonitoringInstance;

import java.util.ArrayList;
import java.util.List;

/** Aggregates each instance independently so a failed metrics source never hides service discovery data. */
public class ApplicationMonitoringService {

    private final ActuatorMetricsClient actuatorClient;

    public ApplicationMonitoringService(ActuatorMetricsClient actuatorClient) {
        this.actuatorClient = actuatorClient;
    }

    /** Collect a stable snapshot for all supplied instances of one application. */
    public List<ApplicationInstanceMonitoring> snapshots(
            String serviceName, List<MonitoringInstance> instances) {
        List<ApplicationInstanceMonitoring> results = new ArrayList<>();
        for (MonitoringInstance instance : instances) {
            try {
                results.add(new ApplicationInstanceMonitoring(serviceName, instance.instanceId(),
                        instance.healthy(), actuatorClient.fetch(serviceName, instance), null));
            } catch (IllegalStateException | IllegalArgumentException unavailable) {
                results.add(new ApplicationInstanceMonitoring(serviceName, instance.instanceId(),
                        instance.healthy(), null, unavailable.getMessage()));
            }
        }
        return List.copyOf(results);
    }
}
