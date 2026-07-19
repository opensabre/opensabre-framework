package io.github.opensabre.governance.usage;

/**
 * 通知场景或通知模板发送量的语义化入口。
 */
public class NotificationUsageRecorder {

    private final UsageCounterRecorder delegate;

    public NotificationUsageRecorder(UsageCounterRecorder delegate) {
        this.delegate = delegate;
    }

    public void sceneSendAttempt(String sceneCode) {
        delegate.attempt(UsageEventTypes.NOTIFICATION_SCENE, sceneCode, UsageEventTypes.NOTIFICATION_SEND);
    }

    public void sceneSendSuccess(String sceneCode) {
        delegate.success(UsageEventTypes.NOTIFICATION_SCENE, sceneCode, UsageEventTypes.NOTIFICATION_SEND);
    }

    public void sceneSendFailure(String sceneCode) {
        delegate.failure(UsageEventTypes.NOTIFICATION_SCENE, sceneCode, UsageEventTypes.NOTIFICATION_SEND);
    }

    public void templateSendAttempt(String templateId) {
        delegate.attempt(UsageEventTypes.NOTIFICATION_TEMPLATE, templateId, UsageEventTypes.NOTIFICATION_SEND);
    }

    public void templateSendSuccess(String templateId) {
        delegate.success(UsageEventTypes.NOTIFICATION_TEMPLATE, templateId, UsageEventTypes.NOTIFICATION_SEND);
    }

    public void templateSendFailure(String templateId) {
        delegate.failure(UsageEventTypes.NOTIFICATION_TEMPLATE, templateId, UsageEventTypes.NOTIFICATION_SEND);
    }
}
