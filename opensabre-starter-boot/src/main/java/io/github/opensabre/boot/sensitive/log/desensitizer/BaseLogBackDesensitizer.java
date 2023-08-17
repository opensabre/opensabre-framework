package io.github.opensabre.boot.sensitive.log.desensitizer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.github.opensabre.boot.sensitive.log.LogBackCoreConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * 默认的脱敏器
 * 在动态脱敏器没有初使化前，默认使用该脱敏器，主要对于系统启动时可能存在的敏感信息，如密码等，不处理业务信息。
 *
 * @author zhoutaoo
 */
@Slf4j
public class BaseLogBackDesensitizer extends AbstractLogBackDesensitizer {

    @Override
    public boolean support(ILoggingEvent event) {
        return true;
    }

    @Override
    public String desensitizing(ILoggingEvent event, String originStr) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(originStr);
        return buffer.toString();
    }
}