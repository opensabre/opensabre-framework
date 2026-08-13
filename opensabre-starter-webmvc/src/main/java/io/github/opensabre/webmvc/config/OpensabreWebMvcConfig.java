package io.github.opensabre.webmvc.config;

import io.github.opensabre.webmvc.exception.DefaultWebMvcExceptionHandlerAdvice;
import io.github.opensabre.webmvc.rest.RestResponseBodyAdvice;
import io.github.opensabre.boot.config.OpensabreServiceConfig;
import io.github.opensabre.boot.config.OpensabreSwaggerConfig;
import io.github.opensabre.webmvc.interceptor.UserInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Opensabre WebMvc auto-configuration.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@PropertySource(value = "classpath:opensabre-webmvc.properties", encoding = "UTF8")
@Import({DefaultWebMvcExceptionHandlerAdvice.class, RestResponseBodyAdvice.class,
        OpensabreServiceConfig.class, OpensabreSwaggerConfig.class})
public class OpensabreWebMvcConfig {

    @Bean
    @ConditionalOnMissingBean
    public UserInterceptor opensabreUserInterceptor() {
        return new UserInterceptor();
    }

    @Bean
    public WebMvcConfigurer opensabreUserContextWebMvcConfigurer(UserInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor);
            }
        };
    }
}
