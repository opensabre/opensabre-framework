package io.github.opensabre.boot.sensitive.log.desensitizer;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * logback脱敏器
 *
 * @author zhoutaoo
 */
public interface LogBackDesensitizer {

    /**
     * 脱敏接口定义
     *
     * @param event  事件
     */
    String desensitize(final ILoggingEvent event);
}
