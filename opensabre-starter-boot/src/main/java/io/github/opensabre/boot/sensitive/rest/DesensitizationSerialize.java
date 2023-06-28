package io.github.opensabre.boot.sensitive.rest;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.DesensitizedUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import io.github.opensabre.boot.annotations.Desensitization;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * 脱敏数据处理类
 */
@NoArgsConstructor
@AllArgsConstructor
public class DesensitizationSerialize extends JsonSerializer<String> implements ContextualSerializer {
    /**
     * 脱敏策略
     */
    private DesensitizationTypeEnum type;
    /**
     * 开始掩码位置
     */
    private Integer startInclude;
    /**
     * 掩码结束位置
     */
    private Integer endExclude;

    @Override
    public void serialize(String str, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(desensitizing(str));
    }

    /**
     * 脱敏处理
     *
     * @param str 原字符
     * @return 脱敏后的字符
     */
    private String desensitizing(String str) {
        switch (type) {
            // 自定义类型脱敏
            case CUSTOM:
                return CharSequenceUtil.hide(str, startInclude, endExclude);
            // userId脱敏
            case USER_ID:
                return String.valueOf(DesensitizedUtil.userId());
            // 中文姓名脱敏
            case CHINESE_NAME:
                return DesensitizedUtil.chineseName(String.valueOf(str));
            // 身份证脱敏
            case ID_CARD:
                return DesensitizedUtil.idCardNum(String.valueOf(str), 1, 2);
            // 固定电话脱敏
            case FIXED_PHONE:
                return DesensitizedUtil.fixedPhone(String.valueOf(str));
            // 手机号脱敏
            case MOBILE_PHONE:
                return DesensitizedUtil.mobilePhone(String.valueOf(str));
            // 地址脱敏
            case ADDRESS:
                return DesensitizedUtil.address(String.valueOf(str), 8);
            // 邮箱脱敏
            case EMAIL:
                return DesensitizedUtil.email(String.valueOf(str));
            // 密码脱敏
            case PASSWORD:
                return DesensitizedUtil.password(String.valueOf(str));
            // 中国车牌脱敏
            case CAR_LICENSE:
                return DesensitizedUtil.carLicense(String.valueOf(str));
            // 银行卡脱敏
            case BANK_CARD:
                return DesensitizedUtil.bankCard(String.valueOf(str));
            default:
                return StringUtils.EMPTY;
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        if (beanProperty != null) {
            // 判断数据类型是否为String类型
            if (Objects.equals(beanProperty.getType().getRawClass(), String.class)) {
                // 获取定义的注解
                Desensitization desensitization = beanProperty.getContextAnnotation(Desensitization.class);
                // 不为null
                if (desensitization != null) {
                    // 创建定义的序列化类的实例并且返回，入参为注解定义的type,开始位置，结束位置。
                    return new DesensitizationSerialize(desensitization.type(), desensitization.startInclude(), desensitization.endExclude());
                }
            }
            return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
        }
        return serializerProvider.findNullValueSerializer(null);
    }
}