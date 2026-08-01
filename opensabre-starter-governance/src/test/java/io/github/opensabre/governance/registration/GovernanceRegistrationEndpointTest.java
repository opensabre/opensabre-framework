package io.github.opensabre.governance.registration;

import io.github.opensabre.governance.config.GovernanceProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GovernanceRegistrationEndpointTest {

    private final ThreadPoolTaskScheduler scheduler = scheduler();

    @AfterEach
    void shutdownScheduler() {
        scheduler.shutdown();
    }

    @Test
    void triggersKnownTaskAndReturnsItsStatus() throws Exception {
        GovernanceRegistrationCoordinator coordinator = new GovernanceRegistrationCoordinator(
                scheduler,
                new GovernanceProperties(),
                new StaticListableBeanFactory().getBeanProvider(io.micrometer.core.instrument.MeterRegistry.class));
        CountDownLatch completed = new CountDownLatch(1);
        GovernanceRegistrationEndpoint endpoint = new GovernanceRegistrationEndpoint(
                coordinator,
                Map.of("dictionary", () -> coordinator.submit("dictionary", completed::countDown)));

        endpoint.trigger("dictionary");

        assertThat(completed.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(endpoint.status()).containsKey("dictionary");
    }

    @Test
    void rejectsUnknownTask() {
        GovernanceRegistrationCoordinator coordinator = new GovernanceRegistrationCoordinator(
                scheduler,
                new GovernanceProperties(),
                new StaticListableBeanFactory().getBeanProvider(io.micrometer.core.instrument.MeterRegistry.class));
        GovernanceRegistrationEndpoint endpoint =
                new GovernanceRegistrationEndpoint(coordinator, Map.of());

        assertThatThrownBy(() -> endpoint.trigger("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown");
    }

    private static ThreadPoolTaskScheduler scheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("governance-registration-endpoint-test-");
        scheduler.initialize();
        return scheduler;
    }
}
