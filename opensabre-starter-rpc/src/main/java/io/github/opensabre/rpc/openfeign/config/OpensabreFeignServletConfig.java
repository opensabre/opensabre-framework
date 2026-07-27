package io.github.opensabre.rpc.openfeign.config;

import io.github.opensabre.rpc.openfeign.interceptor.FeignHeaderInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Servlet-only Feign header forwarding configuration.
 */
@AutoConfiguration(after = OpensabreFeignConfig.class)
@ConditionalOnClass(HttpServletRequest.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class OpensabreFeignServletConfig {

    @Bean
    public FeignHeaderInterceptor feignHeaderInterceptor() {
        return new FeignHeaderInterceptor();
    }
}
