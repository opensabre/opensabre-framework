package io.github.opensabre.governance.registration;

import java.time.Instant;

/**
 * Immutable operational view of one governance registration task.
 */
public record GovernanceRegistrationStatus(
        String task,
        State state,
        int attempt,
        long successes,
        long failures,
        Instant lastStartedAt,
        Instant lastCompletedAt,
        Instant lastSuccessAt,
        Instant lastFailureAt,
        Instant nextRetryAt,
        long lastDurationMillis,
        String lastError) {

    /**
     * Registration lifecycle exposed through the actuator endpoint.
     */
    public enum State {
        IDLE,
        RUNNING,
        RETRY_SCHEDULED,
        SUCCEEDED,
        FAILED
    }
}
