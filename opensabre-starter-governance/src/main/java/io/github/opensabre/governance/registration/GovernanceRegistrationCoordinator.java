package io.github.opensabre.governance.registration;

import io.github.opensabre.governance.config.GovernanceProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.TaskScheduler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runs governance registration on a managed scheduler with bounded exponential retry.
 */
@Slf4j
public class GovernanceRegistrationCoordinator {

    private final TaskScheduler scheduler;
    private final GovernanceProperties.Registration properties;
    private final MeterRegistry meterRegistry;
    private final Clock clock;
    private final Map<String, TaskState> states = new ConcurrentHashMap<>();

    public GovernanceRegistrationCoordinator(
            TaskScheduler scheduler,
            GovernanceProperties properties,
            ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this(scheduler, properties.getRegistration(), meterRegistryProvider.getIfAvailable(), Clock.systemUTC());
    }

    GovernanceRegistrationCoordinator(
            TaskScheduler scheduler,
            GovernanceProperties.Registration properties,
            MeterRegistry meterRegistry,
            Clock clock) {
        this.scheduler = scheduler;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        this.clock = clock;
        validate(properties);
    }

    /**
     * Starts a registration cycle unless the same task is already running or waiting to retry.
     *
     * @param taskName stable task identifier used in logs and metrics
     * @param action registration call that throws when the attempt fails
     * @return {@code true} when a new cycle was accepted
     */
    public boolean submit(String taskName, Runnable action) {
        TaskState state = states.computeIfAbsent(taskName, ignored -> new TaskState(taskName));
        if (!state.inFlight.compareAndSet(false, true)) {
            log.debug("Skip overlapping governance registration task: task={}", taskName);
            return false;
        }
        state.beginCycle();
        scheduleSafely(taskName, action, state, 1, Duration.ZERO);
        return true;
    }

    /**
     * Returns a stable snapshot suitable for actuator and diagnostics.
     */
    public Map<String, GovernanceRegistrationStatus> statuses() {
        Map<String, GovernanceRegistrationStatus> snapshot = new LinkedHashMap<>();
        states.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> snapshot.put(entry.getKey(), entry.getValue().snapshot()));
        return snapshot;
    }

    private void scheduleSafely(
            String taskName,
            Runnable action,
            TaskState state,
            int attempt,
            Duration delay) {
        Instant scheduledAt = clock.instant().plus(delay);
        state.scheduled(attempt, scheduledAt, delay.isZero());
        try {
            scheduler.schedule(() -> runAttempt(taskName, action, state, attempt), scheduledAt);
        } catch (RuntimeException exception) {
            state.schedulingRejected(clock.instant(), exception);
            count(taskName, "rejected");
            log.warn(
                    "Governance registration task could not be scheduled; startup is unaffected: task={}",
                    taskName, exception);
        }
    }

    private void runAttempt(String taskName, Runnable action, TaskState state, int attempt) {
        Instant startedAt = clock.instant();
        state.running(attempt, startedAt);
        Timer.Sample timer = meterRegistry == null ? null : Timer.start(meterRegistry);
        try {
            action.run();
            state.succeeded(clock.instant());
            count(taskName, "success");
            log.info("Governance registration task succeeded: task={}, attempt={}", taskName, attempt);
        } catch (Exception exception) {
            Instant failedAt = clock.instant();
            state.failedAttempt(failedAt, exception);
            count(taskName, "failure");
            if (attempt < properties.getMaxAttempts()) {
                Duration delay = retryDelay(attempt);
                log.warn(
                        "Governance registration task failed; retry scheduled: task={}, attempt={}, delay={}",
                        taskName, attempt, delay, exception);
                scheduleSafely(taskName, action, state, attempt + 1, delay);
            } else {
                state.exhausted();
                log.warn(
                        "Governance registration task failed after all attempts: task={}, attempts={}",
                        taskName, attempt, exception);
            }
        } finally {
            if (timer != null) {
                timer.stop(Timer.builder("opensabre.governance.registration.duration")
                        .description("Governance registration attempt duration")
                        .tag("task", taskName)
                        .register(meterRegistry));
            }
        }
    }

    private Duration retryDelay(int failedAttempt) {
        long initialMillis = properties.getInitialBackoff().toMillis();
        long maximumMillis = properties.getMaxBackoff().toMillis();
        long exponential;
        try {
            exponential = Math.multiplyExact(initialMillis, 1L << Math.min(failedAttempt - 1, 30));
        } catch (ArithmeticException exception) {
            exponential = maximumMillis;
        }
        long capped = Math.min(exponential, maximumMillis);
        double jitter = properties.getJitter();
        double jitterFactor = jitter == 0.0d
                ? 1.0d
                : 1.0d + ThreadLocalRandom.current().nextDouble(-jitter, jitter);
        return Duration.ofMillis(Math.max(0L, Math.round(capped * jitterFactor)));
    }

    private void count(String taskName, String result) {
        if (meterRegistry != null) {
            Counter.builder("opensabre.governance.registration.attempts")
                    .description("Governance registration attempts")
                    .tag("task", taskName)
                    .tag("result", result)
                    .register(meterRegistry)
                    .increment();
        }
    }

    private static void validate(GovernanceProperties.Registration properties) {
        if (properties.getMaxAttempts() < 1) {
            throw new IllegalArgumentException("registration.max-attempts must be at least 1");
        }
        if (properties.getInitialBackoff().isNegative()
                || properties.getMaxBackoff().compareTo(properties.getInitialBackoff()) < 0) {
            throw new IllegalArgumentException("registration backoff configuration is invalid");
        }
        if (properties.getJitter() < 0.0d || properties.getJitter() > 1.0d) {
            throw new IllegalArgumentException("registration.jitter must be between 0 and 1");
        }
        if (properties.getPoolSize() < 1) {
            throw new IllegalArgumentException("registration.pool-size must be at least 1");
        }
        if (properties.getAwaitTermination().isNegative()) {
            throw new IllegalArgumentException("registration.await-termination must not be negative");
        }
    }

    private static final class TaskState {
        private final String taskName;
        private final AtomicBoolean inFlight = new AtomicBoolean();
        private GovernanceRegistrationStatus.State state = GovernanceRegistrationStatus.State.IDLE;
        private int attempt;
        private long successes;
        private long failures;
        private Instant lastStartedAt;
        private Instant lastCompletedAt;
        private Instant lastSuccessAt;
        private Instant lastFailureAt;
        private Instant nextRetryAt;
        private long lastDurationMillis;
        private String lastError;

        private TaskState(String taskName) {
            this.taskName = taskName;
        }

        private synchronized void beginCycle() {
            attempt = 0;
            nextRetryAt = null;
            lastError = null;
        }

        private synchronized void scheduled(int attempt, Instant scheduledAt, boolean immediate) {
            this.attempt = attempt;
            this.state = immediate
                    ? GovernanceRegistrationStatus.State.RUNNING
                    : GovernanceRegistrationStatus.State.RETRY_SCHEDULED;
            this.nextRetryAt = immediate ? null : scheduledAt;
        }

        private synchronized void running(int attempt, Instant startedAt) {
            this.attempt = attempt;
            this.state = GovernanceRegistrationStatus.State.RUNNING;
            this.lastStartedAt = startedAt;
            this.nextRetryAt = null;
        }

        private synchronized void succeeded(Instant completedAt) {
            this.state = GovernanceRegistrationStatus.State.SUCCEEDED;
            this.successes++;
            this.lastCompletedAt = completedAt;
            this.lastSuccessAt = completedAt;
            this.lastDurationMillis = durationMillis(completedAt);
            this.lastError = null;
            this.inFlight.set(false);
        }

        private synchronized void failedAttempt(Instant failedAt, Exception exception) {
            this.failures++;
            this.lastCompletedAt = failedAt;
            this.lastFailureAt = failedAt;
            this.lastDurationMillis = durationMillis(failedAt);
            // The actuator view never exposes backend response text or credentials.
            this.lastError = exception.getClass().getSimpleName();
        }

        private synchronized void exhausted() {
            this.state = GovernanceRegistrationStatus.State.FAILED;
            this.nextRetryAt = null;
            this.inFlight.set(false);
        }

        private synchronized void schedulingRejected(Instant failedAt, RuntimeException exception) {
            failedAttempt(failedAt, exception);
            this.state = GovernanceRegistrationStatus.State.FAILED;
            this.nextRetryAt = null;
            this.inFlight.set(false);
        }

        private long durationMillis(Instant completedAt) {
            return lastStartedAt == null ? 0L : Math.max(0L, Duration.between(lastStartedAt, completedAt).toMillis());
        }

        private synchronized GovernanceRegistrationStatus snapshot() {
            return new GovernanceRegistrationStatus(
                    taskName, state, attempt, successes, failures, lastStartedAt, lastCompletedAt,
                    lastSuccessAt, lastFailureAt, nextRetryAt, lastDurationMillis, lastError);
        }
    }
}
