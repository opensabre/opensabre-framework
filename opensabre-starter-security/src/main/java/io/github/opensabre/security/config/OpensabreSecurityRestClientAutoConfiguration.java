package io.github.opensabre.security.config;

import io.github.opensabre.security.restclient.HostInternalTokenTargetResolver;
import io.github.opensabre.security.restclient.InternalTokenClientHttpRequestInterceptor;
import io.github.opensabre.security.restclient.InternalTokenTargetResolver;
import io.github.opensabre.security.token.InternalTokenRequestFactory;
import io.github.opensabre.security.token.InternalTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/**
 * Servlet-side synchronous HTTP integration for internal token re-signing.
 */
@AutoConfiguration(after = OpensabreSecurityAutoConfiguration.class)
@ConditionalOnClass({RestClient.class, RestClientCustomizer.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class OpensabreSecurityRestClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public InternalTokenTargetResolver internalTokenTargetResolver() {
        return new HostInternalTokenTargetResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public InternalTokenClientHttpRequestInterceptor internalTokenClientHttpRequestInterceptor(
            InternalTokenService tokenService,
            InternalTokenRequestFactory requestFactory,
            InternalTokenTargetResolver targetResolver,
            InternalTokenProperties properties,
            @Value("${spring.application.name:}") String applicationName) {
        return new InternalTokenClientHttpRequestInterceptor(
                tokenService, requestFactory, targetResolver, properties, applicationName);
    }

    @Bean
    public RestClientCustomizer internalTokenRestClientCustomizer(
            InternalTokenClientHttpRequestInterceptor interceptor) {
        return builder -> builder.requestInterceptor(interceptor);
    }
}
