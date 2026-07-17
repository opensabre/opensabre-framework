package io.github.opensabre.persistence.config;

import io.github.opensabre.persistence.exception.PersistenceExceptionHandlerAdvice;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceWebMvcAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PersistenceWebMvcAutoConfiguration.class);

    @Test
    void shouldNotRegisterMvcAdviceOutsideServletApplication() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(PersistenceExceptionHandlerAdvice.class));
    }
}
