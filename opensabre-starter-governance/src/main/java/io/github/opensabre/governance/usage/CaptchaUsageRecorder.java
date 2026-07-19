package io.github.opensabre.governance.usage;

/**
 * 验证码场景使用量的语义化入口。
 */
public class CaptchaUsageRecorder {

    private final UsageCounterRecorder delegate;

    public CaptchaUsageRecorder(UsageCounterRecorder delegate) {
        this.delegate = delegate;
    }

    public void generateAttempt(String sceneCode) {
        delegate.attempt(UsageEventTypes.CAPTCHA_SCENE, sceneCode, UsageEventTypes.CAPTCHA_GENERATE);
    }

    public void generateSuccess(String sceneCode) {
        delegate.success(UsageEventTypes.CAPTCHA_SCENE, sceneCode, UsageEventTypes.CAPTCHA_GENERATE);
    }

    public void generateFailure(String sceneCode) {
        delegate.failure(UsageEventTypes.CAPTCHA_SCENE, sceneCode, UsageEventTypes.CAPTCHA_GENERATE);
    }

    public void verifyAttempt(String sceneCode) {
        delegate.attempt(UsageEventTypes.CAPTCHA_SCENE, sceneCode, UsageEventTypes.CAPTCHA_VERIFY);
    }

    public void verifySuccess(String sceneCode) {
        delegate.success(UsageEventTypes.CAPTCHA_SCENE, sceneCode, UsageEventTypes.CAPTCHA_VERIFY);
    }

    public void verifyFailure(String sceneCode) {
        delegate.failure(UsageEventTypes.CAPTCHA_SCENE, sceneCode, UsageEventTypes.CAPTCHA_VERIFY);
    }
}
