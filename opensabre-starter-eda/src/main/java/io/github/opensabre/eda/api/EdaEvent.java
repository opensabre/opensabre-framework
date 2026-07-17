package io.github.opensabre.eda.api;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * OpenSabre 事件的不可变信封。
 *
 * @param eventId     用于日志关联和消费者幂等的事件标识
 * @param eventType   稳定的业务事件类型，不应使用 Java 类名
 * @param occurredAt  事件产生时间
 * @param producer    生产者应用标识
 * @param traceId     可选链路关联标识
 * @param headers     受控的扩展元数据
 * @param payload     业务载荷
 * @param <T>         业务载荷类型
 */
public record EdaEvent<T>(String eventId, String eventType, Instant occurredAt, String producer,
                          String traceId, Map<String, String> headers, T payload) {

    public EdaEvent {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(producer, "producer must not be null");
        if (eventType.isBlank()) {
            throw new IllegalArgumentException("eventType must not be blank");
        }
        if (producer.isBlank()) {
            throw new IllegalArgumentException("producer must not be blank");
        }
        headers = headers == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(headers));
    }

    /**
     * 创建使用 UUID 和当前时间的事件信封。
     */
    public static <T> EdaEvent<T> of(String eventType, String producer, T payload) {
        return new EdaEvent<>(UUID.randomUUID().toString(), eventType, Instant.now(), producer, null, Map.of(), payload);
    }
}
