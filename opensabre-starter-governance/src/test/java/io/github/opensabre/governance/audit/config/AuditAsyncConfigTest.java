package io.github.opensabre.governance.audit.config;

import io.github.opensabre.governance.config.GovernanceProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditAsyncConfigTest {

    @Test
    void shouldApplyAsyncPropertiesAndExposeMetrics() {
        GovernanceProperties properties = new GovernanceProperties();
        properties.getAudit().getAsync().setCorePoolSize(1);
        properties.getAudit().getAsync().setMaxPoolSize(3);
        properties.getAudit().getAsync().setQueueCapacity(5);
        properties.getAudit().getAsync().setThreadNamePrefix("audit-test-");
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("meterRegistry", meterRegistry);

        ThreadPoolTaskExecutor executor = new AuditAsyncConfig().auditTaskExecutor(properties,
                beanFactory.getBeanProvider(MeterRegistry.class));

        assertEquals(1, executor.getCorePoolSize());
        assertEquals(3, executor.getMaxPoolSize());
        assertEquals(5, executor.getThreadPoolExecutor().getQueue().remainingCapacity());
        assertTrue(executor.getThreadNamePrefix().startsWith("audit-test-"));
        assertEquals(0D, meterRegistry.get("opensabre.governance.audit.executor.active").gauge().value());
        executor.shutdown();
        meterRegistry.close();
    }

    @Test
    void shouldCountRejectedAuditEvents() throws Exception {
        GovernanceProperties properties = new GovernanceProperties();
        properties.getAudit().getAsync().setCorePoolSize(1);
        properties.getAudit().getAsync().setMaxPoolSize(1);
        properties.getAudit().getAsync().setQueueCapacity(0);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("meterRegistry", meterRegistry);
        ThreadPoolTaskExecutor executor = new AuditAsyncConfig().auditTaskExecutor(properties,
                beanFactory.getBeanProvider(MeterRegistry.class));
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        executor.execute(() -> {
            started.countDown();
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });
        assertTrue(started.await(1, TimeUnit.SECONDS));

        executor.execute(() -> { });

        assertEquals(1D, meterRegistry.get("opensabre.governance.audit.events.rejected").counter().count());
        release.countDown();
        executor.shutdown();
        meterRegistry.close();
    }
}
