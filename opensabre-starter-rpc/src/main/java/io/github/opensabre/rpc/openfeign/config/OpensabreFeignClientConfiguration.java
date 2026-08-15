package io.github.opensabre.rpc.openfeign.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.List;
import org.springframework.http.converter.HttpMessageConverter;

/**
 * Feign child-context configuration shared by OpenSabre clients.
 *
 * <p>Spring Cloud OpenFeign 5.0.x publishes its mutable converter list before
 * initialization is complete. Synchronizing access prevents a concurrent first
 * request from observing that list while it is still empty.</p>
 */
public class OpensabreFeignClientConfiguration {

    @Bean
    public FeignHttpMessageConverters opensabreFeignHttpMessageConverters(
            ObjectProvider<ClientHttpMessageConvertersCustomizer> customizers,
            ObjectProvider<HttpMessageConverterCustomizer> cloudCustomizers) {
        return new SynchronizedFeignHttpMessageConverters(customizers, cloudCustomizers);
    }

    private static final class SynchronizedFeignHttpMessageConverters
            extends FeignHttpMessageConverters {

        private SynchronizedFeignHttpMessageConverters(
                ObjectProvider<ClientHttpMessageConvertersCustomizer> customizers,
                ObjectProvider<HttpMessageConverterCustomizer> cloudCustomizers) {
            super(customizers, cloudCustomizers);
        }

        @Override
        public synchronized List<HttpMessageConverter<?>> getConverters() {
            return super.getConverters();
        }
    }
}
