package io.github.opensabre.governance.registration;

import io.github.opensabre.governance.config.GovernanceProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.core.task.TaskRejectedException;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GovernanceRegistrationCoordinatorTest {

    private final ThreadPoolTaskScheduler scheduler = scheduler();

    @AfterEach
    void shutdownScheduler() {
        scheduler.shutdown();
    }

    @Test
    void retriesUntilRegistrationSucceedsAndExposesStatus() throws Exception {
        GovernanceProperties.Registration properties = properties(3);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        GovernanceRegistrationCoordinator coordinator = new GovernanceRegistrationCoordinator(
                scheduler, properties, meterRegistry, Clock.systemUTC());
        AtomicInteger attempts = new AtomicInteger();
        CountDownLatch completed = new CountDownLatch(1);

        assertThat(coordinator.submit("error-catalog", () -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IllegalStateException("sysadmin unavailable");
            }
            completed.countDown();
        })).isTrue();

        assertThat(completed.await(2, TimeUnit.SECONDS)).isTrue();
        awaitState(coordinator, "error-catalog", GovernanceRegistrationStatus.State.SUCCEEDED);
        GovernanceRegistrationStatus status = coordinator.statuses().get("error-catalog");
        assertThat(status.attempt()).isEqualTo(3);
        assertThat(status.successes()).isEqualTo(1);
        assertThat(status.failures()).isEqualTo(2);
        assertThat(status.lastError()).isNull();
        assertThat(meterRegistry.get("opensabre.governance.registration.attempts")
                .tag("task", "error-catalog")
                .tag("result", "success")
                .counter().count()).isEqualTo(1.0d);
    }

    @Test
    void exhaustsBoundedAttemptsWithoutBlockingCaller() throws Exception {
        GovernanceRegistrationCoordinator coordinator = new GovernanceRegistrationCoordinator(
                scheduler, properties(2), null, Clock.systemUTC());
        AtomicInteger attempts = new AtomicInteger();

        assertThat(coordinator.submit("dictionary", () -> {
            attempts.incrementAndGet();
            throw new IllegalStateException("sysadmin unavailable");
        })).isTrue();

        awaitState(coordinator, "dictionary", GovernanceRegistrationStatus.State.FAILED);
        assertThat(attempts).hasValue(2);
        assertThat(coordinator.statuses().get("dictionary").failures()).isEqualTo(2);
    }

    @Test
    void rejectsOverlappingRegistrationCycles() throws Exception {
        GovernanceRegistrationCoordinator coordinator = new GovernanceRegistrationCoordinator(
                scheduler, properties(1), null, Clock.systemUTC());
        CountDownLatch running = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        assertThat(coordinator.submit("dictionary", () -> {
            running.countDown();
            try {
                release.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(exception);
            }
        })).isTrue();
        assertThat(running.await(2, TimeUnit.SECONDS)).isTrue();

        assertThat(coordinator.submit("dictionary", () -> {
        })).isFalse();
        release.countDown();
        awaitState(coordinator, "dictionary", GovernanceRegistrationStatus.State.SUCCEEDED);
    }

    @Test
    void releasesTaskAndRecordsMetricWhenSchedulingIsRejected() {
        TaskScheduler rejectingScheduler = mock(TaskScheduler.class);
        when(rejectingScheduler.schedule(any(Runnable.class), any(java.time.Instant.class)))
                .thenThrow(new TaskRejectedException("scheduler stopped"));
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        GovernanceRegistrationCoordinator coordinator = new GovernanceRegistrationCoordinator(
                rejectingScheduler, properties(1), meterRegistry, Clock.systemUTC());

        assertThat(coordinator.submit("dictionary", () -> {
        })).isTrue();
        assertThat(coordinator.statuses().get("dictionary").state())
                .isEqualTo(GovernanceRegistrationStatus.State.FAILED);
        assertThat(coordinator.statuses().get("dictionary").lastError())
                .isEqualTo("TaskRejectedException");
        assertThat(coordinator.submit("dictionary", () -> {
        })).isTrue();
        assertThat(meterRegistry.get("opensabre.governance.registration.attempts")
                .tag("task", "dictionary")
                .tag("result", "rejected")
                .counter().count()).isEqualTo(2.0d);
    }

    private static void awaitState(
            GovernanceRegistrationCoordinator coordinator,
            String task,
            GovernanceRegistrationStatus.State expected) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline) {
            GovernanceRegistrationStatus status = coordinator.statuses().get(task);
            if (status != null && status.state() == expected) {
                return;
            }
            Thread.sleep(10L);
        }
        assertThat(coordinator.statuses().get(task).state()).isEqualTo(expected);
    }

    private static GovernanceProperties.Registration properties(int maxAttempts) {
        GovernanceProperties.Registration properties = new GovernanceProperties.Registration();
        properties.setMaxAttempts(maxAttempts);
        properties.setInitialBackoff(Duration.ofMillis(1));
        properties.setMaxBackoff(Duration.ofMillis(1));
        properties.setJitter(0.0d);
        return properties;
    }

    private static ThreadPoolTaskScheduler scheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("governance-registration-test-");
        scheduler.initialize();
        return scheduler;
    }
}
