package io.github.opensabre.boot.sensitive.log.desensitizer;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * 日志脱敏处理器抽象类
 *
 * @author zhoutaoo
 */
public abstract class AbstractLogBackDesensitizer implements LogBackDesensitizer {
    /**
     * 如果实现类支持，再执行脱敏的动作
     *
     * @param event 日志事件
     * @return 脱敏后的字符
     */
    @Override
    public String desensitize(ILoggingEvent event, String originStr) {
        if (support(event))
            return desensitizing(event, originStr);
        return originStr;
    }

    /**
     * 脱敏执行
     *
     * @param originStr 日志信息
     * @return 脱敏后的字符
     */
    public abstract String desensitizing(ILoggingEvent event, String originStr);
}
