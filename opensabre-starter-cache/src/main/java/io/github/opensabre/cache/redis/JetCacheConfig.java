package io.github.opensabre.cache.redis;

import com.alicp.jetcache.autoconfigure.JetCacheAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;

/**
 * 打开Redis缓存配置类
 */
@AutoConfiguration(before = JetCacheAutoConfiguration.class)
@PropertySource(value = "classpath:opensabre-cache.properties", encoding = "UTF8")
public class JetCacheConfig {
}