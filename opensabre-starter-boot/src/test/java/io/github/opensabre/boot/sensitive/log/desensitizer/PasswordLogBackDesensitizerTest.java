package io.github.opensabre.boot.sensitive.log.desensitizer;

import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PasswordLogBackDesensitizerTest {

    @Test
    void desensitizing() {
        PasswordLogBackDesensitizer passwordLogBackDesensitizer = new PasswordLogBackDesensitizer();
        LoggingEvent loggingEvent = new LoggingEvent();
        assertEquals("password  :  *******", passwordLogBackDesensitizer.desensitizing(loggingEvent, "password  :  1234567"));
        assertEquals("PASSWORD :*******", passwordLogBackDesensitizer.desensitizing(loggingEvent, "PASSWORD :1234567"));
        assertEquals("password:************", passwordLogBackDesensitizer.desensitizing(loggingEvent, "password:1qaz@WSXsdff"));
        assertEquals("password=************", passwordLogBackDesensitizer.desensitizing(loggingEvent, "password=1qaz@WSXsdff"));
        assertEquals("password>************", passwordLogBackDesensitizer.desensitizing(loggingEvent, "password>1qaz@WSXsdff"));
        assertEquals("Password:************", passwordLogBackDesensitizer.desensitizing(loggingEvent, "Password:1qaz@WSXsdff"));
        assertEquals("PASSWORD:************", passwordLogBackDesensitizer.desensitizing(loggingEvent, "PASSWORD:1qaz@WSXsdff"));
        assertEquals("PASSWD:************", passwordLogBackDesensitizer.desensitizing(loggingEvent, "PASSWD:1qaz@WSXsdff"));
        assertEquals("pass:************", passwordLogBackDesensitizer.desensitizing(loggingEvent, "pass:1qaz@WSXsdff"));
        assertEquals("PASS:************", passwordLogBackDesensitizer.desensitizing(loggingEvent, "PASS:1qaz@WSXsdff"));
        assertEquals("key:********", passwordLogBackDesensitizer.desensitizing(loggingEvent, "key:12345678"));
        assertEquals("KEY:********", passwordLogBackDesensitizer.desensitizing(loggingEvent, "KEY:12345678"));
        assertEquals("TOKEN:********", passwordLogBackDesensitizer.desensitizing(loggingEvent, "TOKEN:12345678"));
        assertEquals("Token:********", passwordLogBackDesensitizer.desensitizing(loggingEvent, "Token:12345678"));
        assertEquals("credential:********", passwordLogBackDesensitizer.desensitizing(loggingEvent, "credential:12345678"));
        assertEquals("secret:********", passwordLogBackDesensitizer.desensitizing(loggingEvent, "secret:12345678"));
        assertEquals("secret:*********", passwordLogBackDesensitizer.desensitizing(loggingEvent, "secret:12345678="));
        assertEquals("secret: *********", passwordLogBackDesensitizer.desensitizing(loggingEvent, "secret: 12345678="));
        assertEquals("Secret : *********", passwordLogBackDesensitizer.desensitizing(loggingEvent, "Secret : 12345678="));
        assertEquals("this a Secret : ********", passwordLogBackDesensitizer.desensitizing(loggingEvent, "this a Secret : 12345678"));
        assertEquals("this a Secret : ******** please change it", passwordLogBackDesensitizer.desensitizing(loggingEvent, "this a Secret : 12345678 please change it"));
        assertEquals("this a Secret : ********* please change it", passwordLogBackDesensitizer.desensitizing(loggingEvent, "this a Secret : 12345678, please change it"));
    }
}