package io.github.opensabre.boot.sensitive.rest.strategy;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.opensabre.boot.sensitive.rule.SensitiveRule;

public class CustomSensitiveStrategy implements SensitiveStrategy {

    private final SensitiveRule sensitiveRule;

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
        return CharSequenceUtil.replace(str,
                sensitiveRule.retainPrefixCount(),
                str.length() - sensitiveRule.retainSuffixCount(),
                sensitiveRule.replaceChar());
    }
}