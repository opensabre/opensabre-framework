package io.github.opensabre.governance.registration;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import java.util.Map;

/**
 * Actuator endpoint exposing governance registration status.
 */
@Endpoint(id = "opensabreGovernanceRegistration")
public class GovernanceRegistrationEndpoint {

    private final GovernanceRegistrationCoordinator coordinator;
    private final Map<String, Runnable> triggers;

    public GovernanceRegistrationEndpoint(
            GovernanceRegistrationCoordinator coordinator,
            Map<String, Runnable> triggers) {
        this.coordinator = coordinator;
        this.triggers = triggers;
    }

    /**
     * Returns the latest status for all known registration tasks.
     */
    @ReadOperation
    public Map<String, GovernanceRegistrationStatus> status() {
        return coordinator.statuses();
    }

    /**
     * Triggers one registration task without allowing overlap.
     */
    @WriteOperation
    public Map<String, GovernanceRegistrationStatus> trigger(String task) {
        Runnable trigger = triggers.get(task);
        if (trigger == null) {
            throw new IllegalArgumentException("Unknown governance registration task: " + task);
        }
        trigger.run();
        return coordinator.statuses();
    }
}
