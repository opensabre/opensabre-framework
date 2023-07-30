package io.github.opensabre.boot.sensitive.log.desensitizer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.github.opensabre.boot.sensitive.rule.DefaultSensitiveRule;
import io.github.opensabre.boot.sensitive.rule.SensitiveRule;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;

/**
 * 密码的脱敏器
 * 在动态脱敏器没有初使化前，默认使用该脱敏器，主要对于系统启动时可能存在的敏感信息，如密码等，不处理业务信息。
 *
 * @author zhoutaoo
 */
public class PasswordLogBackDesensitizer extends AbstractLogBackDesensitizer {
    /**
     * password默认规则
     */
    private final SensitiveRule sensitiveRule = DefaultSensitiveRule.PASSWORD;

    /**
     * 判断日志内容中是否包含密码等字样
     *
     * @param event 日志事件
     * @return true/false
     */
    @Override
    public boolean support(ILoggingEvent event) {
        return sensitiveRule.pattern().matcher(event.getFormattedMessage()).find();
    }

    /**
     * 将内容中密码/凭证等关键词后 密码内容替换为*
     *
     * @param event     日志事件
     * @param originStr 日志原始内容
     * @return 密码/凭证 脱敏后内容
     */
    @Override
    public String desensitizing(ILoggingEvent event, String originStr) {
        AtomicReference<String> message = new AtomicReference<>(originStr);
        Matcher matcher = sensitiveRule.pattern().matcher(originStr);
        while (matcher.find()) {
            String passwd = matcher.group(3);
            message.set(message.get().replaceAll(passwd, sensitiveRule.replace(passwd)));
        }
        return message.get();
    }
}