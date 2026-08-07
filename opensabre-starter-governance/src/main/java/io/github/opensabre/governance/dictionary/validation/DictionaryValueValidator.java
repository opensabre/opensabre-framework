package io.github.opensabre.governance.dictionary.validation;

import io.github.opensabre.governance.dictionary.DictionaryService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 使用统一 {@link DictionaryService} 校验请求字段，复用其启用项和缓存语义。
 */
public class DictionaryValueValidator implements ConstraintValidator<DictionaryValue, Object> {

    private final DictionaryService dictionaryService;
    private String dictCode;

    /**
     * 创建字典值校验器。
     *
     * @param dictionaryService Framework 字典读取服务
     */
    public DictionaryValueValidator(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Override
    public void initialize(DictionaryValue constraintAnnotation) {
        dictCode = constraintAnnotation.value();
    }

    /**
     * 空值交给标准空值约束；非空值必须属于字典启用项。
     *
     * @param value 待校验值
     * @param context 校验上下文
     * @return 是否有效
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null || value instanceof CharSequence text && text.toString().isBlank()) {
            return true;
        }
        return dictionaryService.contains(dictCode, value);
    }
}
