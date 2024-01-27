package io.github.opensabre.rpc.openfeign.config;

import io.github.opensabre.boot.config.YamlPropertyLoaderFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@PropertySource(value = {"classpath:opensabre-rpc.yml"}, encoding = "UTF8", factory = YamlPropertyLoaderFactory.class)
public class OpensabreFeignConfig {
}
