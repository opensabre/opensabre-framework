package io.github.opensabre.security.config;

import io.github.opensabre.security.actuator.ActuatorMonitoringWebFilter;
import io.github.opensabre.security.token.InternalTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.WebFilter;

/** Reactive integration for the shared Actuator internal-token access contract. */
@AutoConfiguration(after = OpensabreSecurityAutoConfiguration.class)
@ConditionalOnClass(WebFilter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class OpensabreSecurityWebFluxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ActuatorMonitoringWebFilter actuatorMonitoringWebFilter(
            InternalTokenService tokenService,
            InternalTokenProperties properties,
            @Value("${spring.application.name:}") String applicationName) {
        return new ActuatorMonitoringWebFilter(tokenService, properties, applicationName);
    }
}
