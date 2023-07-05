package io.github.opensabre.boot.sensitive.log;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.github.opensabre.boot.sensitive.log.desensitizer.DefaultLogBackDesensitizer;
import io.github.opensabre.boot.sensitive.log.desensitizer.LogBackDesensitizer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

/**
 * 日志支持核心转换器
 *
 * @author zhoutaoo
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LogBackCoreConverter extends MessageConverter {
    /**
     * 默认的脱敏器
     */
    private LogBackDesensitizer desensitizer = new DefaultLogBackDesensitizer();

    @Override
    public String convert(ILoggingEvent event) {
        String formattedMessage = event.getFormattedMessage();
        if (StringUtils.isBlank(formattedMessage)) {
            return StringUtils.EMPTY;
        }
        return desensitizer.desensitize(event);
    }
}