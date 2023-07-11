package io.github.opensabre.boot.sensitive.log.strategy;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.common.collect.Lists;
import io.github.opensabre.boot.sensitive.log.desensitizer.LogBackDesensitizer;
import io.github.opensabre.boot.sensitive.log.desensitizer.PasswordLogBackDesensitizer;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 默认脱敏策略
 * 可支持多个脱敏器，循环使用全部脱敏器处理一次
 */
public class DefaultDesensitizerStrategy implements DesensitizerStrategy {
    /**
     * 默认的脱敏器
     */
    private List<LogBackDesensitizer> desensitizers = Lists.newArrayList(new PasswordLogBackDesensitizer());

    @Override
    public String desensitizing(ILoggingEvent event) {
        AtomicReference<String> message = new AtomicReference<>(event.getFormattedMessage());
        desensitizers.forEach(desensitizer -> {
            message.set(desensitizer.desensitize(event, message.get()));
        });
        return message.get();
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