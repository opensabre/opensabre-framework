package io.github.opensabre.webmvc.config;

import io.github.opensabre.webmvc.interceptor.UserInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

class OpensabreWebMvcConfigTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WebMvcAutoConfiguration.class))
            .withUserConfiguration(OpensabreWebMvcConfig.class);

    @Test
    void registersUserContextInterceptorForEveryServletApplication() {
        contextRunner.run(context -> {
            assertThat(context.getBeansOfType(UserInterceptor.class)).hasSize(1);
            assertThat(context.getBeansOfType(WebMvcConfigurer.class).values())
                    .anyMatch(configurer -> configurer.getClass().getName()
                            .contains("OpensabreWebMvcConfig"));
        });
    }
}
