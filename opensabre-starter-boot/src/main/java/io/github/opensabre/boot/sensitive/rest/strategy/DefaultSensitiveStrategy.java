package io.github.opensabre.boot.sensitive.rest.strategy;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.opensabre.boot.sensitive.rule.DefaultSensitiveRule;
import io.github.opensabre.boot.sensitive.rule.SensitiveRule;

import java.util.Arrays;

import static io.github.opensabre.boot.sensitive.rule.DefaultSensitiveRule.values;

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
        return CharSequenceUtil.replace(str,
                sensitiveRule.retainPrefixCount(),
                str.length() - sensitiveRule.retainSuffixCount(),
                sensitiveRule.replaceChar());
    }
}