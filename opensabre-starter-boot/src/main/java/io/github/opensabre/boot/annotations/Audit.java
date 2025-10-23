package io.github.opensabre.boot.annotations;

import java.lang.annotation.*;

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