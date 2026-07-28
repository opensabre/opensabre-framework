package io.github.opensabre.rpc.openfeign.interceptor;

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FeignHeaderInterceptorTest {

    @AfterEach
    void resetRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldNotForwardExternalOrInternalCredentials() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer external");
        request.addHeader("x-client-token", "internal");
        request.addHeader("x-client-token-user", "{\"user_name\":\"forged\"}");
        request.addHeader("x-trace-header", "trace");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        RequestTemplate template = new RequestTemplate();

        new FeignHeaderInterceptor().apply(template);

        assertNull(template.headers().get("Authorization"));
        assertNull(template.headers().get("x-client-token"));
        assertNull(template.headers().get("x-client-token-user"));
        assertEquals("trace", template.headers().get("x-trace-header").iterator().next());
    }
}
