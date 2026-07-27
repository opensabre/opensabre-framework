package io.github.opensabre.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabre.security.webmvc.InternalTokenAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;

class OpensabreSecuritySpringSecurityAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner =
            new WebApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(
                            OpensabreSecurityAutoConfiguration.class,
                            OpensabreSecuritySpringSecurityAutoConfiguration.class))
                    .withBean(ObjectMapper.class, ObjectMapper::new)
                    .withPropertyValues("spring.application.name=base-order");

    @Test
    void registersFilterForExplicitSecurityChainUseOnly() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(InternalTokenAuthenticationFilter.class);
            FilterRegistrationBean<?> registration =
                    context.getBean(FilterRegistrationBean.class);
            assertThat(registration.isEnabled()).isFalse();
        });
    }

    @Test
    void remainsLoadableWithoutSpringSecurity() {
        contextRunner
                .withClassLoader(new FilteredClassLoader("org.springframework.security"))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context)
                            .doesNotHaveBean("internalTokenAuthenticationFilter");
                });
    }
}
