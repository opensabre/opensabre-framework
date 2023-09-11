package io.github.opensabre.boot.sensitive.log.desensitizer;

import io.github.opensabre.boot.sensitive.rule.DefaultSensitiveRule;

/**
 * 姓名脱敏器
 * 日志中形如 姓名:张三 / name=李四 等形如此类的中文姓名敏感数据进行屏蔽
 *
 * @author zhoutaoo
 */
public class NameLogBackDesensitizer extends PrefixLogBackDesensitizer {

    public NameLogBackDesensitizer() {
        super(DefaultSensitiveRule.NAME, 3);

    }
}