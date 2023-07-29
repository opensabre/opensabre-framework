package io.github.opensabre.boot.sensitive.rest;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import io.github.opensabre.boot.annotations.Desensitization;
import io.github.opensabre.boot.sensitive.rest.strategy.CustomSensitiveStrategy;
import io.github.opensabre.boot.sensitive.rest.strategy.DefaultSensitiveStrategy;
import io.github.opensabre.boot.sensitive.rest.strategy.SensitiveStrategy;
import io.github.opensabre.boot.sensitive.rule.CustomSensitiveRule;
import io.github.opensabre.boot.sensitive.rule.DefaultSensitiveRule;
import io.github.opensabre.boot.sensitive.rule.SensitiveRule;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * 脱敏数据处理类
 */
@NoArgsConstructor
public class DesensitizationSerialize extends JsonSerializer<String> implements ContextualSerializer {
    /**
     * 脱敏策略
     */
    private SensitiveStrategy sensitiveStrategy;
    /**
     * 脱敏规则
     */
    private SensitiveRule type;

    /**
     * 默认策略
     *
     * @param type 脱敏类型
     */
    public DesensitizationSerialize(SensitiveRule type) {
        this.type = type;
        this.sensitiveStrategy = new DefaultSensitiveStrategy();
    }

    /**
     * 自定义脱敏类型
     *
     * @param retainPrefixCount 保留前缀个数
     * @param retainSuffixCount 保留后缀个数
     */
    public DesensitizationSerialize(int retainPrefixCount, int retainSuffixCount, char replaceChar) {
        CustomSensitiveRule sensitiveRule = new CustomSensitiveRule(retainPrefixCount, retainSuffixCount, replaceChar);
        this.type = sensitiveRule;
        this.sensitiveStrategy = new CustomSensitiveStrategy(sensitiveRule);
    }

    @Override
    public void serialize(String str, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(sensitiveStrategy.desensitizing(type, str));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        if (Objects.isNull(beanProperty)) {
            return serializerProvider.findNullValueSerializer(null);
        }
        // 判断数据类型是否为String类型
        if (Objects.equals(beanProperty.getType().getRawClass(), String.class)) {
            // 获取定义的注解
            Desensitization desensitization = Optional.ofNullable(beanProperty.getAnnotation(Desensitization.class))
                    .orElse(beanProperty.getContextAnnotation(Desensitization.class));
            // 不为null
            if (desensitization != null) {
                // 创建定义的序列化类的实例并且返回，入参为注解定义的type,开始位置，结束位置。
                if (desensitization.type().equals(DefaultSensitiveRule.CUSTOM))
                    return new DesensitizationSerialize(desensitization.retainPrefixCount(),
                            desensitization.retainSuffixCount(),
                            desensitization.replaceChar());
                else
                    return new DesensitizationSerialize(desensitization.type());
            }
        }
        return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
    }
}