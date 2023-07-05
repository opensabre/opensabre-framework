package io.github.opensabre.boot.sensitive.log.desensitizer;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * 默认的脱敏器
 * 在动态脱敏器没有初使化前，默认使用该脱敏器，主要对于系统启动时可能存在的敏感信息，如密码等，不处理业务信息。
 *
 * @author zhoutaoo
 */
public class DefaultLogBackDesensitizer extends AbstractLogBackDesensitizer {

    @Override
    public boolean support(ILoggingEvent event) {
        return true;
    }

    @Override
    public String desensitizing(ILoggingEvent event) {
        String formattedMessage = event.getFormattedMessage();
        StringBuilder buffer = new StringBuilder();
        Object[] objects = event.getArgumentArray();
        buffer.append(formattedMessage);
        return buffer.toString();
    }
}