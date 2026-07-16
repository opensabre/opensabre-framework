package io.github.opensabre.governance.audit.config;

import io.github.opensabre.governance.config.GovernanceProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 审计事件异步执行配置。
 */
@Slf4j
@EnableAsync
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GovernanceProperties.class)
public class AuditAsyncConfig {

    /**
     * 审计日志写入使用独立线程池，线程池满时丢弃并记录日志，避免阻塞业务请求。
     */
    @Bean("auditTaskExecutor")
    public ThreadPoolTaskExecutor auditTaskExecutor(GovernanceProperties properties,
                                                    ObjectProvider<MeterRegistry> meterRegistryProvider) {
        GovernanceProperties.Audit.Async async = properties.getAudit().getAsync();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(async.getCorePoolSize());
        executor.setMaxPoolSize(async.getMaxPoolSize());
        executor.setQueueCapacity(async.getQueueCapacity());
        executor.setThreadNamePrefix(async.getThreadNamePrefix());
        executor.setWaitForTasksToCompleteOnShutdown(async.isWaitForTasksToCompleteOnShutdown());
        MeterRegistry meterRegistry = meterRegistryProvider.getIfAvailable();
        Counter rejectedCounter = meterRegistry == null ? null : Counter.builder("opensabre.governance.audit.events.rejected")
                .description("Number of audit events discarded because the async executor is saturated")
                .register(meterRegistry);
        executor.setRejectedExecutionHandler((task, threadPool) -> {
            if (rejectedCounter != null) {
                rejectedCounter.increment();
            }
            log.warn("Discard audit event because the async executor is saturated");
        });
        executor.initialize();
        if (meterRegistry != null) {
            meterRegistry.gauge("opensabre.governance.audit.executor.active", executor,
                    taskExecutor -> taskExecutor.getActiveCount());
            meterRegistry.gauge("opensabre.governance.audit.executor.queue.size", executor,
                    taskExecutor -> taskExecutor.getThreadPoolExecutor().getQueue().size());
        }
        return executor;
    }
}
