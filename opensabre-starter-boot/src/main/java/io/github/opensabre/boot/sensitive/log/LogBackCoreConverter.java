package io.github.opensabre.boot.sensitive.log;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.github.opensabre.boot.sensitive.log.desensitizer.DefaultLogBackDesensitizer;
import io.github.opensabre.boot.sensitive.log.desensitizer.LogBackDesensitizer;

/**
 * 日志支持核心转换器
 *
 * @author zhoutaoo
 */
public class LogBackCoreConverter extends MessageConverter {

    private LogBackDesensitizer desensitizer = new DefaultLogBackDesensitizer();

    @Override
    public String convert(ILoggingEvent event) {
        return desensitizer.desensitize(event);
    }
}