package io.github.opensabre.persistence.config;

import io.github.opensabre.persistence.exception.PersistenceExceptionHandlerAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;

/**
 * Registers MVC-specific persistence exception mapping only for servlet applications.
 */
@AutoConfiguration(after = MybatisConfig.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(PersistenceExceptionHandlerAdvice.class)
public class PersistenceWebMvcAutoConfiguration {
}
