package io.github.opensabre.rpc.sentinel.config;

import io.github.opensabre.rpc.sentinel.exception.SentinelExceptionHandlerAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnClass(name = "com.alibaba.csp.sentinel.slots.block.BlockException")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import({SentinelExceptionHandlerAdvice.class})
public class OpensabreSentinelConfig {
}
