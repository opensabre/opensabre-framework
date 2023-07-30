package io.github.opensabre.boot.sensitive.log.desensitizer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import cn.hutool.core.text.CharSequenceUtil;
import io.github.opensabre.boot.sensitive.log.LogBackCoreConverter;
import io.github.opensabre.boot.sensitive.rule.DefaultSensitiveRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * 正则脱敏器
 * 包含常用的敏感日志正则，如身份证、银行卡、手机号、姓名
 *
 * @author zhoutaoo
 */
@Slf4j
@Component
@ConditionalOnMissingBean
@ConditionalOnBean({LogBackCoreConverter.class})
public class RegxLogBackDesensitizer extends AbstractLogBackDesensitizer {

    public final Set<DefaultSensitiveRule> sensitiveRules;

    public RegxLogBackDesensitizer() {
        this.sensitiveRules = Arrays.stream(DefaultSensitiveRule.values())
                .filter(rule -> !rule.pattern().pattern().equals("\\*"))
                .filter(rule -> !rule.equals(DefaultSensitiveRule.PASSWORD))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean support(ILoggingEvent event) {
        // 任意匹配即需要脱敏
        return sensitiveRules.stream()
                .anyMatch(sensitiveRule -> sensitiveRule.pattern().matcher(event.getFormattedMessage()).find());
    }

    @Override
    public String desensitizing(ILoggingEvent event, String originStr) {
        AtomicReference<String> message = new AtomicReference<>(originStr);
        sensitiveRules.forEach(sensitiveRule -> {
            Matcher matcher = sensitiveRule.pattern().matcher(originStr);
            while (matcher.find()) {
                String matchStr = matcher.group();
                message.set(message.get().replaceAll(matchStr, sensitiveRule.replace(matchStr)));
            }
        });
        return message.get();
    }
}