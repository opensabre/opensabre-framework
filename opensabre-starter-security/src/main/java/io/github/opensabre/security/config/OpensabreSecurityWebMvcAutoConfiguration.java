package io.github.opensabre.security.config;

import io.github.opensabre.security.context.InternalTokenUserContext;
import io.github.opensabre.security.token.InternalTokenService;
import io.github.opensabre.security.webmvc.InternalTokenMvcInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Servlet integration for OpenSabre internal token verification.
 */
@AutoConfiguration(after = OpensabreSecurityAutoConfiguration.class)
@ConditionalOnClass(HandlerInterceptor.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class OpensabreSecurityWebMvcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public InternalTokenMvcInterceptor internalTokenMvcInterceptor(
            InternalTokenService tokenService,
            InternalTokenUserContext userContext,
            InternalTokenProperties properties,
            @Value("${spring.application.name:}") String applicationName) {
        return new InternalTokenMvcInterceptor(tokenService, userContext, properties, applicationName);
    }

    @Bean
    public WebMvcConfigurer internalTokenWebMvcConfigurer(InternalTokenMvcInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor);
            }
        };
    }
}
