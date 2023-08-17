package io.github.opensabre.boot.sensitive.log.desensitizer;

import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RegxLogBackDesensitizer.class)
@TestPropertySource(properties = {"opensabre.sensitive.log.rules=mobile"})
class RegxLogBackDesensitizerTest {

    @Resource
    private RegxLogBackDesensitizer regxLogBackDesensitizer;

    @Test
    void desensitizing() {
        LoggingEvent loggingEvent = new LoggingEvent();
        String desensitizing = regxLogBackDesensitizer.desensitizing(loggingEvent, "mobile=13612345678");
        assertEquals("mobile=136****5678",desensitizing);
    }
}