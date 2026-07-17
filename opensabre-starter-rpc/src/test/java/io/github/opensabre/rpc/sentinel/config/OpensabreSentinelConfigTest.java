package io.github.opensabre.rpc.sentinel.config;

import io.github.opensabre.rpc.sentinel.exception.SentinelExceptionHandlerAdvice;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class OpensabreSentinelConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(OpensabreSentinelConfig.class);

    @Test
    void shouldNotRegisterMvcAdviceOutsideServletApplication() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(SentinelExceptionHandlerAdvice.class));
    }
}
