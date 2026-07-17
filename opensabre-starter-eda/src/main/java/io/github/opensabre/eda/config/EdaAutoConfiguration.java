package io.github.opensabre.eda.config;

import io.github.opensabre.eda.api.EdaEventPublisher;
import io.github.opensabre.eda.core.DefaultEdaEventPublisher;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * OpenSabre EDA 本地运行时自动配置。
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "opensabre.eda", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(EdaProperties.class)
public class EdaAutoConfiguration {

    /**
     * 创建处理进程内事件的有界线程池。
     */
    @Bean("edaTaskExecutor")
    @ConditionalOnMissingBean(name = "edaTaskExecutor")
    public ThreadPoolTaskExecutor edaTaskExecutor(EdaProperties properties,
                                                  ObjectProvider<MeterRegistry> meterRegistryProvider) {
        EdaProperties.Local local = properties.getLocal();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(local.getCorePoolSize());
        executor.setMaxPoolSize(local.getMaxPoolSize());
        executor.setQueueCapacity(local.getQueueCapacity());
        executor.setThreadNamePrefix(local.getThreadNamePrefix());
        executor.setWaitForTasksToCompleteOnShutdown(local.isWaitForTasksToCompleteOnShutdown());
        MeterRegistry meterRegistry = meterRegistryProvider.getIfAvailable();
        Counter rejectedCounter = meterRegistry == null ? null : Counter.builder("opensabre.eda.executor.rejected")
                .description("Number of EDA local handler tasks rejected by the executor").register(meterRegistry);
        executor.setRejectedExecutionHandler((task, threadPool) -> {
            if (rejectedCounter != null) {
                rejectedCounter.increment();
            }
            throw new java.util.concurrent.RejectedExecutionException("EDA executor is saturated");
        });
        executor.initialize();
        if (meterRegistry != null) {
            meterRegistry.gauge("opensabre.eda.executor.active", executor, ThreadPoolTaskExecutor::getActiveCount);
            meterRegistry.gauge("opensabre.eda.executor.queue.size", executor,
                    taskExecutor -> taskExecutor.getThreadPoolExecutor().getQueue().size());
        }
        return executor;
    }

    /**
     * 创建统一事件发布器；应用可声明同类型 Bean 进行替换。
     */
    @Bean
    @ConditionalOnMissingBean(EdaEventPublisher.class)
    public EdaEventPublisher edaEventPublisher(org.springframework.beans.factory.ObjectProvider<io.github.opensabre.eda.api.EdaEventHandler<?>> handlers,
                                                org.springframework.beans.factory.ObjectProvider<io.github.opensabre.eda.api.EventTransport> transports,
                                                ThreadPoolTaskExecutor edaTaskExecutor, EdaProperties properties,
                                                ObjectProvider<MeterRegistry> meterRegistryProvider) {
        return new DefaultEdaEventPublisher(handlers, transports, edaTaskExecutor, properties, meterRegistryProvider);
    }
}
