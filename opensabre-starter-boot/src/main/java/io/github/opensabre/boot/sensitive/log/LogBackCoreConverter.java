package io.github.opensabre.boot.sensitive.log;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.github.opensabre.boot.sensitive.log.strategy.DefaultDesensitizerStrategy;
import io.github.opensabre.boot.sensitive.log.strategy.DesensitizerStrategy;
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
     * 默认日志脱敏策略
     */
    private DesensitizerStrategy desensitizerStrategy = new DefaultDesensitizerStrategy();

    @Override
    public String convert(ILoggingEvent event) {
        if (StringUtils.isBlank(event.getFormattedMessage()))
            return StringUtils.EMPTY;
        return desensitizerStrategy.desensitizing(event);
    }
}