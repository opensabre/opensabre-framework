package io.github.opensabre.eda.api;

/**
 * 跨服务事件传输 SPI。
 * <p>
 * 应用自行引入 RabbitMQ、Kafka、RocketMQ 等客户端，并通过实现该接口接入；
 * OpenSabre EDA 核心不依赖任何具体消息中间件。
 */
public interface EventTransport {

    /**
     * 将事件发送到远程传输介质。
     */
    void publish(EdaEvent<?> event);
}
