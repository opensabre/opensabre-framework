package io.github.opensabre.security.config;

import io.github.opensabre.security.context.InternalTokenUserContext;
import io.github.opensabre.security.token.InternalTokenService;
import io.github.opensabre.security.webmvc.InternalTokenAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Optional Servlet bridge between verified internal tokens and Spring Security.
 */
@AutoConfiguration(after = OpensabreSecurityWebMvcAutoConfiguration.class)
@ConditionalOnClass({SecurityContextHolder.class, OncePerRequestFilter.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class OpensabreSecuritySpringSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public InternalTokenAuthenticationFilter internalTokenAuthenticationFilter(
            InternalTokenService tokenService,
            InternalTokenUserContext userContext,
            InternalTokenProperties properties,
            @Value("${spring.application.name:}") String applicationName) {
        return new InternalTokenAuthenticationFilter(
                tokenService, userContext, properties, applicationName);
    }

    /**
     * Prevent Boot from registering the security filter outside SecurityFilterChain.
     */
    @Bean
    public FilterRegistrationBean<InternalTokenAuthenticationFilter>
            internalTokenAuthenticationFilterRegistration(
                    InternalTokenAuthenticationFilter filter) {
        FilterRegistrationBean<InternalTokenAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
