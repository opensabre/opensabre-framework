package io.github.opensabre.governance.errorcatalog;

import io.github.opensabre.governance.client.SysadminGovernanceClient;
import io.github.opensabre.governance.config.GovernanceProperties;
import io.github.opensabre.governance.registration.GovernanceRegistrationCoordinator;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorCatalogRegistrationListenerTest {

    private ThreadPoolTaskScheduler scheduler;

    @AfterEach
    void shutdownScheduler() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    @Test
    void registersResolvedSnapshotAfterApplicationIsReady() throws InterruptedException {
        CountDownLatch registered = new CountDownLatch(1);
        SysadminGovernanceClient client = client((snapshot, token) -> registered.countDown());
        ErrorCatalogRegistrationListener listener = listener(client);

        listener.register();

        assertTrue(registered.await(5, TimeUnit.SECONDS));
    }

    @Test
    void remoteFailureDoesNotEscapeTheApplicationReadyListener() throws InterruptedException {
        CountDownLatch attempted = new CountDownLatch(1);
        SysadminGovernanceClient client = client((snapshot, token) -> {
            attempted.countDown();
            throw new IllegalStateException("sysadmin unavailable");
        });
        ErrorCatalogRegistrationListener listener = listener(client);

        assertDoesNotThrow(listener::register);

        assertTrue(attempted.await(5, TimeUnit.SECONDS));
    }

    private ErrorCatalogRegistrationListener listener(SysadminGovernanceClient client) {
        ObjectProvider<SysadminGovernanceClient> clientProvider = new ObjectProvider<>() {
            @Override
            public SysadminGovernanceClient getObject() {
                return client;
            }
        };
        ErrorCatalogProvider provider = () -> List.of(
                new ErrorCatalogEntry("DEMO-001", "Demo error", "demo", 400,
                        true, false, null));
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.application.name", "base-demo")
                .withProperty("opensabre.governance.error-catalog.registration-token",
                        "registration-secret");
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("error-catalog-registration-test-");
        scheduler.initialize();
        ObjectProvider<MeterRegistry> meterRegistryProvider = new ObjectProvider<>() {
            @Override
            public MeterRegistry getObject() {
                return null;
            }
        };
        GovernanceProperties properties = new GovernanceProperties();
        properties.getRegistration().setMaxAttempts(1);
        GovernanceRegistrationCoordinator coordinator = new GovernanceRegistrationCoordinator(
                scheduler, properties, meterRegistryProvider);
        return new ErrorCatalogRegistrationListener(
                clientProvider, List.of(provider), environment, coordinator);
    }

    private SysadminGovernanceClient client(RegistrationCall registrationCall) {
        return (SysadminGovernanceClient) Proxy.newProxyInstance(
                SysadminGovernanceClient.class.getClassLoader(),
                new Class<?>[]{SysadminGovernanceClient.class},
                (proxy, method, arguments) -> {
                    if ("registerErrorCatalog".equals(method.getName())) {
                        registrationCall.invoke(
                                (ErrorCatalogSnapshot) arguments[0], (String) arguments[1]);
                    }
                    return null;
                });
    }

    @FunctionalInterface
    private interface RegistrationCall {
        void invoke(ErrorCatalogSnapshot snapshot, String token);
    }
}
