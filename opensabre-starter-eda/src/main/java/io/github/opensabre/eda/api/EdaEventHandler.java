package io.github.opensabre.eda.api;

/**
 * 事件处理器。实现类应自行保证有副作用处理的幂等性。
 *
 * @param <T> 业务载荷类型
 */
public interface EdaEventHandler<T> {

    /**
     * 返回该处理器订阅的稳定事件类型。
     */
    String eventType();

    /**
     * 处理一个事件信封。
     */
    void handle(EdaEvent<T> event);
}
