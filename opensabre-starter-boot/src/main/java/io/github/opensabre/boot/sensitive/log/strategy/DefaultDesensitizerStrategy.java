package io.github.opensabre.boot.sensitive.log.strategy;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.common.collect.Sets;
import io.github.opensabre.boot.sensitive.log.desensitizer.LogBackDesensitizer;
import io.github.opensabre.boot.sensitive.log.desensitizer.PasswordLogBackDesensitizer;

import java.util.Set;

/**
 * 默认脱敏策略
 * 可支持多个脱敏器，循环使用全部脱敏器处理一次
 */
public class DefaultDesensitizerStrategy implements DesensitizerStrategy {
    /**
     * 默认的脱敏器
     */
    private final Set<LogBackDesensitizer> desensitizers = Sets.newHashSet(new PasswordLogBackDesensitizer());

    @Override
    public String desensitizing(ILoggingEvent event) {
        final String[] message = {event.getFormattedMessage()};
        desensitizers.forEach(desensitizer -> {
            message[0] = desensitizer.desensitize(event, message[0]);
        });
        return message[0];
    }

    /**
     * 追回脱敏器
     *
     * @param logBackDesensitizer 脱敏器
     */
    public void addDesensitizer(LogBackDesensitizer logBackDesensitizer) {
        desensitizers.add(logBackDesensitizer);
    }
}