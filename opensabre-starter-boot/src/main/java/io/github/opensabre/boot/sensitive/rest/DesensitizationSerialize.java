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
     * 脱敏类型
     */
    private DesensitizationTypeEnum type;

    /**
     * 默认策略
     *
     * @param type 脱敏类型
     */
    public DesensitizationSerialize(DesensitizationTypeEnum type) {
        this.type = type;
        this.sensitiveStrategy = new DefaultSensitiveStrategy();
    }

    /**
     * 自定义脱敏类型
     *
     * @param startInclude 开始掩码位置
     * @param endExclude   掩码结束位置
     */
    public DesensitizationSerialize(int startInclude, int endExclude) {
        this.type = DesensitizationTypeEnum.CUSTOM;
        this.sensitiveStrategy = new CustomSensitiveStrategy(startInclude, endExclude);
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
                if (DesensitizationTypeEnum.CUSTOM.equals(desensitization.type()))
                    return new DesensitizationSerialize(desensitization.startInclude(), desensitization.endExclude());
                else
                    return new DesensitizationSerialize(desensitization.type());
            }
        }
        return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
    }
}