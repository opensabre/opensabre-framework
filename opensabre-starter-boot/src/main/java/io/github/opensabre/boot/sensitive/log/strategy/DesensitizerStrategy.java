package io.github.opensabre.boot.sensitive.log.strategy;

import ch.qos.logback.classic.spi.ILoggingEvent;

public interface DesensitizerStrategy {
    /**
     * @param event 日志事件
     * @return 脱敏后的日志字串
     */
    String desensitizing(ILoggingEvent event);
}
