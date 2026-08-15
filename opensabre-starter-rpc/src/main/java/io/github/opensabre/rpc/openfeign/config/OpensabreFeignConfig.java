package io.github.opensabre.rpc.openfeign.config;

import io.github.opensabre.boot.config.YamlPropertyLoaderFactory;
import io.github.opensabre.rpc.openfeign.interceptor.FeignInternalTokenInterceptor;
import io.github.opensabre.security.config.InternalTokenProperties;
import io.github.opensabre.security.token.InternalTokenRequestFactory;
import io.github.opensabre.security.token.InternalTokenService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import tools.jackson.databind.json.JsonMapper;

@AutoConfiguration
@PropertySource(value = {"classpath:opensabre-rpc.yml"}, encoding = "UTF8", factory = YamlPropertyLoaderFactory.class)
public class OpensabreFeignConfig {

    @Bean
    @ConditionalOnBean(JsonMapper.class)
    public HttpMessageConverterCustomizer opensabreFeignJackson3HttpMessageConverter(
            JsonMapper jsonMapper) {
        return converters -> {
            boolean jackson3Configured = converters.stream()
                    .anyMatch(JacksonJsonHttpMessageConverter.class::isInstance);
            if (!jackson3Configured) {
                converters.add(new JacksonJsonHttpMessageConverter(jsonMapper));
            }
        };
    }

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
