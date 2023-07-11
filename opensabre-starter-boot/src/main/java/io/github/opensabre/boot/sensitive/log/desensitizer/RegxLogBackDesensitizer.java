package io.github.opensabre.boot.sensitive.log.desensitizer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.github.opensabre.boot.sensitive.log.LogBackCoreConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * 正则脱敏器
 * 包含常用的敏感日志正则，如身份证、银行卡、手机号、姓名
 *
 * @author zhoutaoo
 */
@Slf4j
@Component
@ConditionalOnMissingBean
@ConditionalOnBean(LogBackCoreConverter.class)
public class RegxLogBackDesensitizer extends AbstractLogBackDesensitizer {

    public static final Pattern idCardPattern = Pattern.compile("(\\D)(\\d{4})(\\d{2})(19|20\\d{8})(\\d[0-9Xx])(\\D)");
    public static final Pattern bankCardPattern = Pattern.compile("(\\D)([3-6]\\d{3})(\\d{8,12})(\\d{4})(\\D)");
    public static final Pattern mobilePattern = Pattern.compile("(\\D)(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])(\\d{4})(\\d{4})(\\D)");
    public static final Pattern namePattern = Pattern.compile("([^\\u4e00-\\u9fa5])([\\u4e00-\\u9fa5])([\\u4e00-\\u9fa5]{1,3})([^\\u4e00-\\u9fa5])");

    @Override
    public boolean support(ILoggingEvent event) {
        return true;
    }

    @Override
    public String desensitizing(ILoggingEvent event, String originStr) {
        String[] patterns = new String[]{idCardPattern.pattern(), bankCardPattern.pattern(), mobilePattern.pattern(), namePattern.pattern()};
        String[] replacements = new String[]{"$1$2************$5$6", "$1$2********$4$5", "$1$2****$4$5", "$1$2**$4"};
        AtomicReference<String> message = new AtomicReference<>(originStr);
        for (int i = 0; i < patterns.length; i++) {
            message.set(message.get().replaceAll(patterns[i], replacements[i]));
        }
        return message.get();
    }
}