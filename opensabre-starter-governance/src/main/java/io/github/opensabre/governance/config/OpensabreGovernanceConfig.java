package io.github.opensabre.governance.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabre.boot.config.YamlPropertyLoaderFactory;
import io.github.opensabre.eda.api.EdaEventPublisher;
import io.github.opensabre.governance.audit.aspect.AuditAspect;
import io.github.opensabre.governance.audit.event.DefaultAuditEventHandler;
import io.github.opensabre.governance.client.SysadminGovernanceClient;
import io.github.opensabre.governance.errorcatalog.ErrorCatalogProvider;
import io.github.opensabre.governance.errorcatalog.ErrorCatalogRegistrationListener;
import io.github.opensabre.governance.dictionary.DictionaryProvider;
import io.github.opensabre.governance.dictionary.ClasspathDictionaryEnumProvider;
import io.github.opensabre.governance.dictionary.DictionaryRegistrationListener;
import io.github.opensabre.governance.dictionary.DictionaryService;
import io.github.opensabre.governance.dictionary.JetCacheDictionaryService;
import io.github.opensabre.governance.dictionary.DictionaryPreloadListener;
import io.github.opensabre.governance.registration.GovernanceRegistrationCoordinator;
import io.github.opensabre.governance.registration.GovernanceRegistrationEndpoint;
import com.alicp.jetcache.CacheManager;
import io.github.opensabre.governance.ratelimit.aspect.RateLimitAspect;
import io.github.opensabre.governance.ratelimit.GovernanceRateLimiter;
import io.github.opensabre.governance.ratelimit.HttpGovernanceRateLimiter;
import io.github.opensabre.governance.usage.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.core.io.ResourceLoader;
import org.springframework.context.annotation.PropertySource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import io.github.opensabre.common.core.exception.SystemErrorType;
import java.util.List;
import java.util.Map;

@AutoConfiguration
@EnableFeignClients(basePackageClasses = SysadminGovernanceClient.class)
@EnableConfigurationProperties(GovernanceProperties.class)
@PropertySource(value = "classpath:opensabre-governance.yml", encoding = "UTF8", factory = YamlPropertyLoaderFactory.class)
@ConditionalOnProperty(prefix = "opensabre.governance", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OpensabreGovernanceConfig {

    @Bean("governanceRegistrationScheduler")
    @ConditionalOnMissingBean(name = "governanceRegistrationScheduler")
    public ThreadPoolTaskScheduler governanceRegistrationScheduler(GovernanceProperties properties) {
        GovernanceProperties.Registration registration = properties.getRegistration();
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(registration.getPoolSize());
        scheduler.setThreadNamePrefix(registration.getThreadNamePrefix());
        scheduler.setWaitForTasksToCompleteOnShutdown(registration.isWaitForTasksToCompleteOnShutdown());
        scheduler.setAwaitTerminationSeconds((int) registration.getAwaitTermination().toSeconds());
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }

    @Bean
    @ConditionalOnMissingBean
    public GovernanceRegistrationCoordinator governanceRegistrationCoordinator(
            @Qualifier("governanceRegistrationScheduler")
            ThreadPoolTaskScheduler governanceRegistrationScheduler,
            GovernanceProperties properties,
            ObjectProvider<io.micrometer.core.instrument.MeterRegistry> meterRegistryProvider) {
        return new GovernanceRegistrationCoordinator(
                governanceRegistrationScheduler, properties, meterRegistryProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "opensabre.governance.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AuditAspect auditAspect(ObjectMapper objectMapper, EdaEventPublisher eventPublisher) {
        return new AuditAspect(objectMapper, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "opensabre.governance.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public DefaultAuditEventHandler defaultAuditEventHandler(ObjectProvider<SysadminGovernanceClient> clientProvider) {
        return new DefaultAuditEventHandler(clientProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "opensabre.governance.ratelimit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public GovernanceRateLimiter governanceRateLimiter(SysadminGovernanceClient client, GovernanceProperties properties) {
        return new HttpGovernanceRateLimiter(client, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "opensabre.governance.ratelimit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RateLimitAspect rateLimitAspect(GovernanceRateLimiter rateLimiter) {
        return new RateLimitAspect(rateLimiter);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "opensabre.governance.usage", name = "transport", havingValue = "EDA", matchIfMissing = true)
    public UsageCounterRecorder edaUsageCounterRecorder(EdaEventPublisher eventPublisher) {
        return new EdaUsageCounterRecorder(eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "opensabre.governance.usage", name = "transport", havingValue = "HTTP")
    public UsageCounterRecorder httpUsageCounterRecorder(SysadminGovernanceClient client) {
        return new HttpUsageCounterRecorder(client);
    }

    @Bean @ConditionalOnMissingBean
    public CaptchaUsageRecorder captchaUsageRecorder(UsageCounterRecorder recorder) { return new CaptchaUsageRecorder(recorder); }
    @Bean @ConditionalOnMissingBean
    public RateLimitUsageRecorder rateLimitUsageRecorder(UsageCounterRecorder recorder) { return new RateLimitUsageRecorder(recorder); }
    @Bean @ConditionalOnMissingBean
    public NotificationUsageRecorder notificationUsageRecorder(UsageCounterRecorder recorder) { return new NotificationUsageRecorder(recorder); }

    @Bean
    @ConditionalOnMissingBean(name = "systemErrorCatalogProvider")
    public ErrorCatalogProvider systemErrorCatalogProvider() {
        return ErrorCatalogProvider.common("opensabre-framework", "framework", SystemErrorType.values());
    }

    @Bean
    @ConditionalOnProperty(prefix = "opensabre.governance.error-catalog", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ErrorCatalogRegistrationListener errorCatalogRegistrationListener(ObjectProvider<SysadminGovernanceClient> clientProvider,
            List<ErrorCatalogProvider> providers, org.springframework.core.env.Environment environment,
            GovernanceRegistrationCoordinator registrationCoordinator) {
        return new ErrorCatalogRegistrationListener(
                clientProvider, providers, environment, registrationCoordinator);
    }

    @Bean
    @ConditionalOnProperty(prefix = "opensabre.governance.dictionary", name = "registration-enabled",
            havingValue = "true")
    public DictionaryRegistrationListener dictionaryRegistrationListener(
            ObjectProvider<SysadminGovernanceClient> clientProvider,
            List<DictionaryProvider> providers,
            org.springframework.core.env.Environment environment,
            GovernanceRegistrationCoordinator registrationCoordinator) {
        return new DictionaryRegistrationListener(
                clientProvider, providers, environment, registrationCoordinator);
    }

    @Bean
    @ConditionalOnProperty(prefix = "opensabre.governance.dictionary", name = "enabled",
            havingValue = "true", matchIfMissing = true)
    public DictionaryProvider dictionaryEnumProvider(
            ResourceLoader resourceLoader,
            BeanFactory beanFactory,
            GovernanceProperties properties) {
        List<String> scanPackages = properties.getDictionary().getScanPackages();
        if (scanPackages == null || scanPackages.isEmpty()) {
            scanPackages = AutoConfigurationPackages.has(beanFactory)
                    ? AutoConfigurationPackages.get(beanFactory)
                    : List.of();
        }
        return new ClasspathDictionaryEnumProvider(resourceLoader, scanPackages);
    }

    @Bean
    @ConditionalOnMissingBean
    public GovernanceRegistrationEndpoint governanceRegistrationEndpoint(
            GovernanceRegistrationCoordinator coordinator,
            ObjectProvider<ErrorCatalogRegistrationListener> errorCatalogListener,
            ObjectProvider<DictionaryRegistrationListener> dictionaryListener) {
        Map<String, Runnable> triggers = new java.util.LinkedHashMap<>();
        ErrorCatalogRegistrationListener errorCatalog = errorCatalogListener.getIfAvailable();
        DictionaryRegistrationListener dictionary = dictionaryListener.getIfAvailable();
        if (errorCatalog != null) {
            triggers.put("error-catalog", errorCatalog::register);
        }
        if (dictionary != null) {
            triggers.put("dictionary", dictionary::register);
        }
        return new GovernanceRegistrationEndpoint(coordinator, Map.copyOf(triggers));
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "opensabre.governance.dictionary", name = "enabled",
            havingValue = "true", matchIfMissing = true)
    public DictionaryService dictionaryService(CacheManager cacheManager, SysadminGovernanceClient client) {
        return new JetCacheDictionaryService(cacheManager, client);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "opensabre.governance.dictionary", name = "enabled",
            havingValue = "true", matchIfMissing = true)
    public DictionaryPreloadListener dictionaryPreloadListener(
            DictionaryService dictionaryService, GovernanceProperties properties) {
        return new DictionaryPreloadListener(dictionaryService, properties);
    }
}
