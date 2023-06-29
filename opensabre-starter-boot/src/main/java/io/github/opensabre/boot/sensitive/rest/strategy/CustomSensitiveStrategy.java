package io.github.opensabre.boot.sensitive.rest.strategy;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.opensabre.boot.sensitive.rest.DesensitizationTypeEnum;

public class CustomSensitiveStrategy implements SensitiveStrategy {
    /**
     * 开始掩码位置
     */
    private Integer startInclude;
    /**
     * 掩码结束位置
     */
    private Integer endExclude;

    public CustomSensitiveStrategy(Integer startInclude, Integer endExclude) {
        this.startInclude = startInclude;
        this.endExclude = endExclude;
    }

    /**
     * 脱敏处理
     *
     * @param str 原字符
     * @return 脱敏后的字符
     */
    public String desensitizing(DesensitizationTypeEnum type, String str) {
        return CharSequenceUtil.hide(str, startInclude, endExclude);
    }
}