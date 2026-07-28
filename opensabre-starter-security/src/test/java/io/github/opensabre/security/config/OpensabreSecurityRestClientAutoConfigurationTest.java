package io.github.opensabre.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabre.security.restclient.InternalTokenClientHttpRequestInterceptor;
import io.github.opensabre.security.restclient.InternalTokenTargetResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.client.RestClientCustomizer;

import static org.assertj.core.api.Assertions.assertThat;

class OpensabreSecurityRestClientAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    OpensabreSecurityAutoConfiguration.class,
                    OpensabreSecurityRestClientAutoConfiguration.class))
            .withBean(ObjectMapper.class, ObjectMapper::new)
            .withPropertyValues("spring.application.name=base-middle");

    @Test
    void shouldRegisterServletRestClientIntegration() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(InternalTokenClientHttpRequestInterceptor.class);
            assertThat(context).hasSingleBean(InternalTokenTargetResolver.class);
            assertThat(context).hasSingleBean(RestClientCustomizer.class);
        });
    }

    @Test
    void shouldAllowApplicationTargetResolverOverride() {
        InternalTokenTargetResolver resolver = uri -> "mapped-service";

        contextRunner
                .withBean(InternalTokenTargetResolver.class, () -> resolver)
                .run(context -> assertThat(context.getBean(InternalTokenTargetResolver.class))
                        .isSameAs(resolver));
    }
}
