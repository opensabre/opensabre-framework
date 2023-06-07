package io.github.opensabre.common.web.validator;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * 手机号校验判定类
 */
public class MobileValidator implements ConstraintValidator<Mobile, String> {
    /**
     * 中国手机号正则
     */
    public static final String REG_MOBILE = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";

    @Override
    public void initialize(Mobile constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        //为空时不进行校验
        if (StringUtils.isBlank(value))
            return true;
        return Pattern.matches(REG_MOBILE, value);
    }
}