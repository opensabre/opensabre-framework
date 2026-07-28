package io.github.opensabre.rpc.openfeign.config;

import io.github.opensabre.boot.config.YamlPropertyLoaderFactory;
import io.github.opensabre.rpc.openfeign.interceptor.FeignInternalTokenInterceptor;
import io.github.opensabre.security.config.InternalTokenProperties;
import io.github.opensabre.security.token.InternalTokenRequestFactory;
import io.github.opensabre.security.token.InternalTokenService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@PropertySource(value = {"classpath:opensabre-rpc.yml"}, encoding = "UTF8", factory = YamlPropertyLoaderFactory.class)
public class OpensabreFeignConfig {

    @Bean
    public FeignInternalTokenInterceptor feignInternalTokenInterceptor(
            InternalTokenService tokenService,
            InternalTokenRequestFactory requestFactory,
            InternalTokenProperties properties,
            @Value("${spring.application.name:}") String applicationName) {
        return new FeignInternalTokenInterceptor(
                tokenService, requestFactory, properties, applicationName);
    }
}
