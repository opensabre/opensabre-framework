package io.github.opensabre.governance.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabre.boot.config.YamlPropertyLoaderFactory;
import io.github.opensabre.governance.audit.aspect.AuditAspect;
import io.github.opensabre.governance.audit.event.DefaultAuditEventHandler;
import io.github.opensabre.governance.client.SysadminGovernanceClient;
import io.github.opensabre.governance.ratelimit.aspect.RateLimitAspect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@EnableFeignClients(basePackageClasses = SysadminGovernanceClient.class)
@EnableConfigurationProperties(GovernanceProperties.class)
@PropertySource(value = "classpath:opensabre-governance.yml", encoding = "UTF8", factory = YamlPropertyLoaderFactory.class)
@ConditionalOnProperty(prefix = "opensabre.governance", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OpensabreGovernanceConfig {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "opensabre.governance.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AuditAspect auditAspect(ObjectMapper objectMapper) {
        return new AuditAspect(objectMapper);
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
    public RateLimitAspect rateLimitAspect(SysadminGovernanceClient client, GovernanceProperties properties) {
        return new RateLimitAspect(client, properties);
    }
}
