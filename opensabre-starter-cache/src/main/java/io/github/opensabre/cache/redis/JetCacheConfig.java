package io.github.opensabre.cache.redis;

import com.alicp.jetcache.autoconfigure.JetCacheAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.PropertySource;

/**
 * 打开Redis缓存配置类
 */
@AutoConfiguration(before = JetCacheAutoConfiguration.class)
@ConditionalOnClass(JetCacheAutoConfiguration.class)
@ConditionalOnProperty(prefix = "opensabre.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
@PropertySource(value = "classpath:opensabre-cache.properties", encoding = "UTF8")
public class JetCacheConfig {
}
