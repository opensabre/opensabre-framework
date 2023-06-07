package io.github.opensabre.boot.config;

import io.github.opensabre.common.web.exception.DefaultGlobalExceptionHandlerAdvice;
import io.github.opensabre.common.web.rest.RestResponseBodyAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 初使化全局报文和全局异常配置
 */
@AutoConfiguration
@Import({DefaultGlobalExceptionHandlerAdvice.class, RestResponseBodyAdvice.class})
public class OpensabreBootConfig {
}
