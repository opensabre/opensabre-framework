package io.github.opensabre.boot.sensitive.log.desensitizer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.github.opensabre.boot.sensitive.log.LogBackCoreConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * 对象脱敏器
 *
 * @author zhoutaoo
 */
@Slf4j
@Component
@ConditionalOnMissingBean
@ConditionalOnBean(LogBackCoreConverter.class)
public class DefaultLogBackDesensitizer implements LogBackDesensitizer {

    @Override
    public String desensitize(ILoggingEvent event) {
        String formattedMessage = event.getFormattedMessage();
        if (StringUtils.isBlank(formattedMessage)) {
            return StringUtils.EMPTY;
        }
        StringBuilder buffer = new StringBuilder();
        Object[] objects = event.getArgumentArray();
        buffer.append(formattedMessage);
        return buffer.toString();
    }
}