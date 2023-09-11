package io.github.opensabre.boot.sensitive.log.desensitizer;

import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NameLogBackDesensitizerTest {

    @Test
    void desensitizing() {
        NameLogBackDesensitizer nameLogBackDesensitizer = new NameLogBackDesensitizer();
        LoggingEvent loggingEvent = new LoggingEvent();
        assertEquals("name  :  张*", nameLogBackDesensitizer.desensitizing(loggingEvent, "name  :  张三"));
        assertEquals("name:  张**", nameLogBackDesensitizer.desensitizing(loggingEvent, "name:  张三小"));
        assertEquals("name is 张**", nameLogBackDesensitizer.desensitizing(loggingEvent, "name is 张三小"));
        assertEquals("姓名：李**", nameLogBackDesensitizer.desensitizing(loggingEvent, "姓名：李四大"));
        assertEquals("姓名=李**", nameLogBackDesensitizer.desensitizing(loggingEvent, "姓名=李四大"));
        assertEquals("姓名>>李*****", nameLogBackDesensitizer.desensitizing(loggingEvent, "姓名>>李四大买买提"));
        assertEquals("姓名<李*****提", nameLogBackDesensitizer.desensitizing(loggingEvent, "姓名<李四大买买提提"));
    }
}