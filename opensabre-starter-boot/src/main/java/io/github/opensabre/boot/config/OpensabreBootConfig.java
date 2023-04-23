package io.github.opensabre.boot.config;

import io.github.opensabre.common.web.exception.DefaultGlobalExceptionHandlerAdvice;
import io.github.opensabre.common.web.rest.RestResponseBodyAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({DefaultGlobalExceptionHandlerAdvice.class, RestResponseBodyAdvice.class})
public class OpensabreBootConfig {
}
