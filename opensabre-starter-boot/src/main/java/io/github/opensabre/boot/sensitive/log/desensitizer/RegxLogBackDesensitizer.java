package io.github.opensabre.boot.sensitive.log.desensitizer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.common.collect.Sets;
import io.github.opensabre.boot.sensitive.log.LogBackCoreConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
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
@ConditionalOnBean({LogBackCoreConverter.class})
public class RegxLogBackDesensitizer extends AbstractLogBackDesensitizer {

    public static final Pattern idCardPattern = Pattern.compile("(\\d{6})(19|20\\d{9})([Xx])");
    public static final Pattern bankCardPattern = Pattern.compile("([3-6]\\d{3})(\\d{8,12})(\\d{4})");
    public static final Pattern mobilePattern = Pattern.compile("(13[0-9]|14[01456789]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])(\\d{8})");
    public static final Pattern namePattern = Pattern.compile("([^\\u4e00-\\u9fa5])([\\u4e00-\\u9fa5])([\\u4e00-\\u9fa5]{1,3})([^\\u4e00-\\u9fa5])");

    public static final Set<Pattern> patterns = Sets.newHashSet(namePattern, bankCardPattern, mobilePattern, idCardPattern);

    @Override
    public boolean support(ILoggingEvent event) {
        // 任意匹配即需要脱敏
        return patterns.stream().anyMatch(pattern -> pattern.matcher(event.getFormattedMessage()).find());
    }

    @Override
    public String desensitizing(ILoggingEvent event, String originStr) {
        AtomicReference<String> message = new AtomicReference<>(originStr);
        patterns.forEach(pattern -> {
            Matcher matcher = pattern.matcher(originStr);
            while (matcher.find()) {
                String matchStr = matcher.group();
                message.set(message.get().replaceAll(matchStr, matchStr.replaceAll(".", "*")));
            }
        });
        return message.get();
    }
}