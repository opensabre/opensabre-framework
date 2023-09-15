package io.github.opensabre.boot.sensitive.log.desensitizer;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * logback脱敏器
 *
 * @author zhoutaoo
 */
public interface LogBackDesensitizer {
    /**
     * 是否支持脱敏
     *
     * @param event 日志事件
     * @return true/false 支持/不支持
     */
    boolean support(ILoggingEvent event);

    /**
     * 脱敏接口定义
     *
     * @param event     事件
     * @param originStr 原始字串
     * @return 脱敏后的字串
     */
    String desensitize(final ILoggingEvent event, String originStr);
}
