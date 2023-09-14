package io.github.opensabre.boot.annotations;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.opensabre.boot.sensitive.rest.DesensitizationSerialize;
import io.github.opensabre.boot.sensitive.rule.DefaultSensitiveRule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = DesensitizationSerialize.class)
public @interface Desensitization {

    /**
     * 脱敏数据类型，只有在CUSTOM的时候，retainPrefixCount和retainSuffixCount生效
     */
    DefaultSensitiveRule type() default DefaultSensitiveRule.CUSTOM;

    /**
     * 保留前缀个数
     */
    int retainPrefixCount() default 0;

    /**
     * 保留后缀个数
     */
    int retainSuffixCount() default 0;

    /**
     * 掩码符号
     */
    char replaceChar() default '*';
}