package io.github.opensabre.boot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares stable authorization metadata for a Spring MVC endpoint.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourcePermission {
    String code() default "";

    String name() default "";

    String type() default "";

    String description() default "";

    boolean register() default true;
}
