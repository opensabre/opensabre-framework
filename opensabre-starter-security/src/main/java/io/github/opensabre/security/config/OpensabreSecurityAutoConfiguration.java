package io.github.opensabre.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
}
