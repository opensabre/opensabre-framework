package io.github.opensabre.governance.usage;

/**
 * 限次判定的异步观测入口；不能替代同步限次判定。
 */
public class RateLimitUsageRecorder {

    private final UsageCounterRecorder delegate;

    public RateLimitUsageRecorder(UsageCounterRecorder delegate) {
        this.delegate = delegate;
    }

    public void checkAttempt(String sceneCode) {
        delegate.attempt(UsageEventTypes.RATE_LIMIT_SCENE, sceneCode, UsageEventTypes.RATE_LIMIT_CHECK);
    }

    public void allowed(String sceneCode) {
        delegate.success(UsageEventTypes.RATE_LIMIT_SCENE, sceneCode, UsageEventTypes.RATE_LIMIT_CHECK);
    }

    public void rejected(String sceneCode) {
        delegate.failure(UsageEventTypes.RATE_LIMIT_SCENE, sceneCode, UsageEventTypes.RATE_LIMIT_CHECK);
    }
}
