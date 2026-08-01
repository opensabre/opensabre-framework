package io.github.opensabre.config.bootstrap;

import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/** Registers OpenSabre common configuration before Nacos application configuration. */
@Configuration(proxyBeanMethods = false)
public class OpensabreNacosBootstrapConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    PropertySourceLocator opensabreCommonNacosPropertySourceLocator() {
        return new OpensabreCommonNacosPropertySourceLocator();
    }
}
