package io.github.opensabre.governance.dictionary.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 校验字段值是否属于指定字典的启用项。
 *
 * <p>空值由 {@code @NotNull} 或 {@code @NotBlank} 等标准约束负责；字典加载失败时保留
 * {@link io.github.opensabre.governance.dictionary.DictionaryUnavailableException}，避免把基础设施故障
 * 误报为业务值非法。</p>
 *
 * @since 0.7.6
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = DictionaryValueValidator.class)
public @interface DictionaryValue {

    /**
     * 待校验的字典编码。
     *
     * @return 字典编码
     */
    String value();

    /**
     * 校验失败提示。
     *
     * @return 提示信息
     */
    String message() default "必须是字典 {value} 中的启用项";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
