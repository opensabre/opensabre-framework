package io.github.opensabre.governance.audit.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 审计事件异步执行配置。
 */
@Slf4j
@EnableAsync
@Configuration(proxyBeanMethods = false)
public class AuditAsyncConfig {

    /**
     * 审计日志写入使用独立线程池，线程池满时丢弃并记录日志，避免阻塞业务请求。
     */
    @Bean("auditTaskExecutor")
    public ThreadPoolTaskExecutor auditTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(1_000);
        executor.setThreadNamePrefix("audit-event-");
        executor.setRejectedExecutionHandler((task, threadPool) ->
                log.warn("Discard audit event because the async executor is saturated"));
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.initialize();
        return executor;
    }
}
