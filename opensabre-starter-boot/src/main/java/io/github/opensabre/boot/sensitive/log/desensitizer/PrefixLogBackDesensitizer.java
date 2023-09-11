package io.github.opensabre.boot.sensitive.log.desensitizer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.github.opensabre.boot.sensitive.rule.SensitiveRule;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;

/**
 * pattern的脱敏器实现
 * 姓名、密码类敏感信息在日志中没有特别明显的可识别特性，但一般在这些关键信息前都会有一些提示词，
 * 故此实现主要为匹配带有一定特别前缀的敏感信息：
 * name : 张三
 * password = 123456
 *
 * @author zhoutaoo
 */
@Slf4j
public class PrefixLogBackDesensitizer extends AbstractLogBackDesensitizer {
    /**
     * 规则
     */
    private final SensitiveRule sensitiveRule;
    /**
     * 匹配的关键词所在位置
     */
    private final int keywordsGroupIndex;

    public PrefixLogBackDesensitizer(SensitiveRule sensitiveRule, int keywordsGroupIndex) {
        this.sensitiveRule = sensitiveRule;
        this.keywordsGroupIndex = keywordsGroupIndex;
    }

    /**
     * 判断日志内容中是否包含关键词等字样
     *
     * @param event 日志事件
     * @return true/false
     */
    @Override
    public boolean support(ILoggingEvent event) {
        return sensitiveRule.pattern().matcher(event.getFormattedMessage()).find();
    }

    /**
     * 将内容中匹配前缀关键词的内容替换为*
     *
     * @param event     日志事件
     * @param originStr 日志原始内容
     * @return 脱敏后内容
     */
    @Override
    public String desensitizing(ILoggingEvent event, String originStr) {
        AtomicReference<String> message = new AtomicReference<>(originStr);
        Matcher matcher = sensitiveRule.pattern().matcher(originStr);
        while (matcher.find()) {
            String passwd = matcher.group(keywordsGroupIndex);
            message.set(message.get().replaceAll(passwd, sensitiveRule.replace(passwd)));
        }
        return message.get();
    }
}