package io.github.opensabre.governance.dictionary;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个应自动注册到字典中心的枚举。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpenSabreDictionary {

    /**
     * 全局字典编码。
     *
     * @return 字典编码
     */
    String code();

    /**
     * 字典展示名称。
     *
     * @return 字典名称
     */
    String name();
}
