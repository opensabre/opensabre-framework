package io.github.opensabre.cache.redis;

/**
 * 默认缓存区域
 * longTime 长时间缓存
 * shortTime 短时缓存
 */
public interface DefaultCacheArea {
    String LONG_TIME_AREA = "longTime";
    String SHORT_TIME_AREA = "shortTime";
}
