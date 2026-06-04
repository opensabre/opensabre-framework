package io.github.opensabre.webflux.config;

import io.github.opensabre.webflux.exception.DefaultWebFluxExceptionHandlerAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;

/**
 * Opensabre WebFlux auto-configuration.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Import(DefaultWebFluxExceptionHandlerAdvice.class)
public class OpensabreWebFluxConfig {
}
