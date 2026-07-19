package io.github.opensabre.rpc.sentinel.config;

import io.github.opensabre.rpc.sentinel.exception.SentinelExceptionHandlerAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import({SentinelExceptionHandlerAdvice.class})
public class OpensabreSentinelConfig {
}
