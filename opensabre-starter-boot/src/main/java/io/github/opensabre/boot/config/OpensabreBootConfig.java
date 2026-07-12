package io.github.opensabre.boot.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;

/**
 * 初使化 Boot 通用配置
 */
@AutoConfiguration
@PropertySource(value = "classpath:opensabre-boot.properties", encoding = "UTF8")
public class OpensabreBootConfig {
}
