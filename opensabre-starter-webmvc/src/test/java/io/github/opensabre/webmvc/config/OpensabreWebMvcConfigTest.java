package io.github.opensabre.webmvc.config;

import io.github.opensabre.webmvc.interceptor.UserInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OpensabreWebMvcConfigTest {
    @Test
    void registersUserContextInterceptorForEveryServletApplication() {
        OpensabreWebMvcConfig configuration = new OpensabreWebMvcConfig();
        UserInterceptor interceptor = configuration.opensabreUserInterceptor();
        WebMvcConfigurer configurer = configuration.opensabreUserContextWebMvcConfigurer(interceptor);
        InterceptorRegistry registry = mock(InterceptorRegistry.class);

        configurer.addInterceptors(registry);

        verify(registry).addInterceptor(interceptor);
    }
}
