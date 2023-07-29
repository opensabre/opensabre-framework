package io.github.opensabre.boot.sensitive.rest.strategy;

import io.github.opensabre.boot.sensitive.rule.SensitiveRule;

/**
 * 脱敏策略接口
 */
public interface SensitiveStrategy {
    /**
     * 脱敏处理
     *
     * @param type      类型
     * @param originStr 原始字符串
     * @return 脱敏后的字符
     */
    String desensitizing(SensitiveRule type, String originStr);
}
