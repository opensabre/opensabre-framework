package io.github.opensabre.webmvc.config;

import io.github.opensabre.webmvc.exception.DefaultWebMvcExceptionHandlerAdvice;
import io.github.opensabre.webmvc.rest.RestResponseBodyAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;

/**
 * Opensabre WebMvc auto-configuration.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import({DefaultWebMvcExceptionHandlerAdvice.class, RestResponseBodyAdvice.class})
public class OpensabreWebMvcConfig {
}
