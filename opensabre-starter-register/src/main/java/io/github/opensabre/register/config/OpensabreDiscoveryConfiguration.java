package io.github.opensabre.register.config;

import io.github.opensabre.boot.config.YamlPropertyLoaderFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@PropertySource(value = {"classpath:opensabre-register.yml"}, encoding = "UTF8", factory = YamlPropertyLoaderFactory.class)
public class OpensabreDiscoveryConfiguration {
}
