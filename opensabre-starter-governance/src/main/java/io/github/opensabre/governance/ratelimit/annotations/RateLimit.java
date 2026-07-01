package io.github.opensabre.governance.ratelimit.annotations;

import io.github.opensabre.governance.ratelimit.enums.RateLimitAlgorithmType;
import io.github.opensabre.governance.ratelimit.enums.RateLimitDimension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    String sceneCode() default "";

    RateLimitAlgorithmType algorithm() default RateLimitAlgorithmType.COUNTER;

    RateLimitDimension[] dimensions() default {RateLimitDimension.IP};

    int maxCount() default 5;

    int period() default 60;

    String key() default "";

    String keyPrefix() default "";

    boolean enabled() default true;

    String message() default "访问过于频繁，请稍后再试";

    boolean showRemaining() default true;
}
