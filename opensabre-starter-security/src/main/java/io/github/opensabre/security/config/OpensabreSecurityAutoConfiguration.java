package io.github.opensabre.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.alibaba.cloud.nacos.NacosConfigManager;
import io.github.opensabre.security.context.InternalTokenUserContext;
import io.github.opensabre.security.key.InternalTokenKeyStatusProvider;
import io.github.opensabre.security.key.PropertiesInternalTokenKeyStatusProvider;
import io.github.opensabre.security.principal.InternalTokenPrincipalProvider;
import io.github.opensabre.security.principal.SpringSecurityInternalTokenPrincipalProvider;
import io.github.opensabre.security.token.DefaultInternalTokenService;
import io.github.opensabre.security.token.InternalTokenService;
import io.github.opensabre.security.token.InternalTokenRequestFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for OpenSabre internal token services.
 */
@AutoConfiguration
@EnableConfigurationProperties(InternalTokenProperties.class)
public class OpensabreSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public InternalTokenService internalTokenService(
            ObjectMapper objectMapper, InternalTokenProperties properties) {
        return new DefaultInternalTokenService(objectMapper, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public InternalTokenUserContext internalTokenUserContext(ObjectMapper objectMapper) {
        return new InternalTokenUserContext(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public InternalTokenRequestFactory internalTokenRequestFactory(
            InternalTokenUserContext internalTokenUserContext,
            InternalTokenPrincipalProvider principalProvider) {
        return new InternalTokenRequestFactory(internalTokenUserContext, principalProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public InternalTokenPrincipalProvider internalTokenPrincipalProvider() {
        return new SpringSecurityInternalTokenPrincipalProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public InternalTokenKeyStatusProvider internalTokenKeyStatusProvider(
            InternalTokenProperties properties) {
        return new PropertiesInternalTokenKeyStatusProvider(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "opensabre.security.internal-token", name = "enabled", havingValue = "true")
    public InternalTokenConfigurationRefresher internalTokenConfigurationRefresher(
            NacosConfigManager configManager, InternalTokenProperties properties,
            @Value("${OPENSABRE_COMMON_CONFIG_DATA_ID:opensabre-common.yml}") String dataId,
            @Value("${OPENSABRE_COMMON_CONFIG_GROUP:DEFAULT_GROUP}") String group) {
        return new InternalTokenConfigurationRefresher(configManager, properties, dataId, group);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "opensabre.security.internal-token", name = "enabled", havingValue = "true")
    public InternalTokenRefreshEndpoint internalTokenRefreshEndpoint(InternalTokenConfigurationRefresher refresher) {
        return new InternalTokenRefreshEndpoint(refresher);
    }
}
