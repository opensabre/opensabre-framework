package io.github.opensabre.rpc.sentinel.config;

import io.github.opensabre.rpc.sentinel.exception.SentinelExceptionHandlerAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({SentinelExceptionHandlerAdvice.class})
public class OpensabreSentinelConfig {
}