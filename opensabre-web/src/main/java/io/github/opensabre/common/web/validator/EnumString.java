package io.github.opensabre.common.web.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(EnumString.List.class)
@Documented
@Constraint(validatedBy = EnumStringValidator.class)//标明由哪个类执行校验逻辑
public @interface EnumString {
    String message() default "类型只能为 value 列表内的值";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * @return data must in this value array
     */
    String[] value();

    /**
     * Defines several {@link EnumString} annotations on the same element.
     *
     * @see EnumString
     */
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        EnumString[] value();
    }
}