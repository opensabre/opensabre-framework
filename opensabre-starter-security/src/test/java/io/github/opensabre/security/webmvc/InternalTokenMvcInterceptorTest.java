package io.github.opensabre.security.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabre.common.core.util.UserContextHolder;
import io.github.opensabre.security.config.InternalTokenProperties;
import io.github.opensabre.security.context.InternalTokenUserContext;
import io.github.opensabre.security.token.DefaultInternalTokenService;
import io.github.opensabre.security.token.InternalTokenRequest;
import io.github.opensabre.security.token.InternalTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalTokenMvcInterceptorTest {

    @AfterEach
    void clear() {
        UserContextHolder.getInstance().clear();
    }

    @Test
    void shouldBindAndClearVerifiedToken() throws Exception {
        InternalTokenProperties properties = properties();
        InternalTokenService service = new DefaultInternalTokenService(new ObjectMapper(), properties);
        InternalTokenUserContext context = new InternalTokenUserContext(new ObjectMapper());
        InternalTokenMvcInterceptor interceptor =
                new InternalTokenMvcInterceptor(service, context, properties, "base-order");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-client-token", service.issue(request()));

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        assertEquals("user-1", UserContextHolder.getInstance().getUserId());
        assertEquals("zhangsan", UserContextHolder.getInstance().getUsername());

        interceptor.afterCompletion(request, new MockHttpServletResponse(), new Object(), null);
        assertNull(UserContextHolder.getInstance().getUserId());
        assertNull(context.currentClaims());
    }

    @Test
    void shouldRejectInvalidToken() throws Exception {
        InternalTokenProperties properties = properties();
        InternalTokenMvcInterceptor interceptor = new InternalTokenMvcInterceptor(
                new DefaultInternalTokenService(new ObjectMapper(), properties),
                new InternalTokenUserContext(new ObjectMapper()), properties, "base-order");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-client-token", "invalid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertEquals(false, interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
    }

    @Test
    void shouldAllowExternalJwtRequestWhenTokenIsNotRequired() throws Exception {
        InternalTokenProperties properties = properties();
        InternalTokenMvcInterceptor interceptor = new InternalTokenMvcInterceptor(
                new DefaultInternalTokenService(new ObjectMapper(), properties),
                new InternalTokenUserContext(new ObjectMapper()), properties, "base-order");

        assertTrue(interceptor.preHandle(
                new MockHttpServletRequest(), new MockHttpServletResponse(), new Object()));
    }

    @Test
    void shouldRejectMissingTokenForInternalOnlyApplication() throws Exception {
        InternalTokenProperties properties = properties();
        properties.setRequired(true);
        InternalTokenMvcInterceptor interceptor = new InternalTokenMvcInterceptor(
                new DefaultInternalTokenService(new ObjectMapper(), properties),
                new InternalTokenUserContext(new ObjectMapper()), properties, "base-order");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertEquals(false, interceptor.preHandle(new MockHttpServletRequest(), response, new Object()));
        assertEquals(401, response.getStatus());
    }

    @Test
    void shouldBypassValidationWhenGloballyDisabled() throws Exception {
        InternalTokenProperties properties = properties();
        properties.setEnabled(false);
        InternalTokenMvcInterceptor interceptor = new InternalTokenMvcInterceptor(
                new DefaultInternalTokenService(new ObjectMapper(), properties),
                new InternalTokenUserContext(new ObjectMapper()), properties, "base-order");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-client-token", "invalid");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void shouldAllowExcludedHealthPathWhenTokenIsRequired() throws Exception {
        InternalTokenProperties properties = properties();
        properties.setRequired(true);
        InternalTokenMvcInterceptor interceptor = new InternalTokenMvcInterceptor(
                new DefaultInternalTokenService(new ObjectMapper(), properties),
                new InternalTokenUserContext(new ObjectMapper()), properties, "base-order");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    private static InternalTokenProperties properties() {
        InternalTokenProperties properties = new InternalTokenProperties();
        properties.setActiveKeyId("active");
        properties.setActiveKey(Base64.getEncoder().encodeToString(
                "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)));
        properties.setAllowedIssuers(Set.of("base-organization"));
        return properties;
    }

    private static InternalTokenRequest request() {
        return new InternalTokenRequest(
                "base-organization", "user-1", "zhangsan", "base-order",
                List.of("order:read"), List.of("admin"), 1, null, "trace-1", Map.of());
    }
}
