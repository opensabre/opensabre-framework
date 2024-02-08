package io.github.opensabre.config;

import io.github.opensabre.boot.config.YamlPropertyLoaderFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@PropertySource(value = {"classpath:opensabre-config.yml"}, encoding = "UTF8", factory = YamlPropertyLoaderFactory.class)
public class OpensabreConfig {
}
