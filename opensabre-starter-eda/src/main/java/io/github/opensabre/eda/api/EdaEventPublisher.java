package io.github.opensabre.eda.api;

/**
 * 统一的 OpenSabre 事件发布入口。
 */
public interface EdaEventPublisher {

    /**
     * 按指定目标发布事件。
     */
    void publish(EdaEvent<?> event, EventTarget... targets);

    /**
     * 仅在当前应用内异步分发事件。
     */
    default void publishLocal(EdaEvent<?> event) {
        publish(event, EventTarget.LOCAL);
    }

    /**
     * 仅通过已启用的远程传输适配器发布事件。
     */
    default void publishRemote(EdaEvent<?> event) {
        publish(event, EventTarget.REMOTE);
    }

    /**
     * 在事务提交后发布；没有事务时立即发布。
     */
    void publishAfterCommit(EdaEvent<?> event, EventTarget... targets);
}
