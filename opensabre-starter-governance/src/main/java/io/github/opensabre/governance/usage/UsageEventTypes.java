package io.github.opensabre.governance.usage;

/**
 * 内置对象使用类型和事件类型常量。
 */
public final class UsageEventTypes {

    public static final String CAPTCHA_SCENE = "CAPTCHA_SCENE";
    public static final String RATE_LIMIT_SCENE = "RATE_LIMIT_SCENE";
    public static final String NOTIFICATION_SCENE = "NOTIFICATION_SCENE";
    public static final String NOTIFICATION_TEMPLATE = "NOTIFICATION_TEMPLATE";
    public static final String CAPTCHA_GENERATE = "CAPTCHA_GENERATE";
    public static final String CAPTCHA_VERIFY = "CAPTCHA_VERIFY";
    public static final String RATE_LIMIT_CHECK = "RATE_LIMIT_CHECK";
    public static final String NOTIFICATION_SEND = "NOTIFICATION_SEND";
    public static final String EDA_EVENT_TYPE = "governance.usage.recorded";

    private UsageEventTypes() {
    }
}
