package io.github.opensabre.boot.sensitive.log.desensitizer;

import io.github.opensabre.boot.sensitive.rule.DefaultSensitiveRule;

/**
 * 密码的脱敏器
 * 在动态脱敏器没有初使化前，默认使用该脱敏器。
 * 主要对于系统启动时可能存在的敏感信息，如密码/凭证/key等。
 * 如 passwd:123456 / key=123456 等形如此类的敏感数据进行屏蔽
 *
 * @author zhoutaoo
 */
public class PasswordLogBackDesensitizer extends PrefixLogBackDesensitizer {

    public PasswordLogBackDesensitizer() {
        super(DefaultSensitiveRule.PASSWORD, 3);
    }
}