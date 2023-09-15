package io.github.opensabre.boot.sensitive.rest.strategy;

import io.github.opensabre.boot.sensitive.rule.SensitiveRule;

/**
 * 自定义脱敏策略
 */
public class CustomSensitiveStrategy implements SensitiveStrategy {

    private final SensitiveRule sensitiveRule;

    /**
     * @param sensitiveRule 脱敏规则
     */
    public CustomSensitiveStrategy(SensitiveRule sensitiveRule) {
        this.sensitiveRule = sensitiveRule;
    }

    /**
     * 脱敏处理
     *
     * @param str 原字符
     * @return 脱敏后的字符
     */
    public String desensitizing(SensitiveRule type, String str) {
        return sensitiveRule.replace(str);
    }
}