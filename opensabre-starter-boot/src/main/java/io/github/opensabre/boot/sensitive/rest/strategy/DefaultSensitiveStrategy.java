package io.github.opensabre.boot.sensitive.rest.strategy;

import io.github.opensabre.boot.sensitive.rule.DefaultSensitiveRule;
import io.github.opensabre.boot.sensitive.rule.SensitiveRule;

import java.util.Arrays;

import static io.github.opensabre.boot.sensitive.rule.DefaultSensitiveRule.values;

/**
 * 默认的脱敏策略
 */
public class DefaultSensitiveStrategy implements SensitiveStrategy {

    /**
     * 脱敏处理
     *
     * @param str 原字符
     * @return 脱敏后的字符
     */
    public String desensitizing(SensitiveRule type, String str) {
        DefaultSensitiveRule sensitiveRule = Arrays.stream(values()).sequential()
                .filter(rule -> rule.equals(type))
                .findFirst().orElse(DefaultSensitiveRule.CUSTOM);
        return sensitiveRule.replace(str);
    }
}