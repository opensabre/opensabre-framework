package io.github.opensabre.governance.errorcatalog;

import io.github.opensabre.governance.client.SysadminGovernanceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.env.MockEnvironment;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorCatalogRegistrationListenerTest {

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
        return new ErrorCatalogRegistrationListener(clientProvider, List.of(provider), environment);
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
