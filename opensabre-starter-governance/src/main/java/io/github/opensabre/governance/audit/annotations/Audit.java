package io.github.opensabre.governance.audit.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audit {

    /**
     * 操作类型
     */
    OperationType operationType();

    /**
     * 操作描述
     */
    String description();

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 是否记录请求参数
     */
    boolean request() default true;

    /**
     * 是否记录响应结果
     */
    boolean response() default false;

    /**
     * 目标对象关键信息，可支持 spel
     */
    String key() default "";
}
