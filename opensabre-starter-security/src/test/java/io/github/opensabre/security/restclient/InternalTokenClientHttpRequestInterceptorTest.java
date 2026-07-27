package io.github.opensabre.security.restclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabre.common.core.util.UserContextHolder;
import io.github.opensabre.security.config.InternalTokenProperties;
import io.github.opensabre.security.context.InternalTokenUserContext;
import io.github.opensabre.security.token.DefaultInternalTokenService;
import io.github.opensabre.security.token.InternalTokenClaims;
import io.github.opensabre.security.token.InternalTokenConstants;
import io.github.opensabre.security.token.InternalTokenRequest;
import io.github.opensabre.security.token.InternalTokenRequestFactory;
import io.github.opensabre.security.token.InternalTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class InternalTokenClientHttpRequestInterceptorTest {

    @AfterEach
    void clearContext() {
        UserContextHolder.getInstance().clear();
    }

    @Test
    void shouldResignForUriHostAndStripExternalCredentials() throws Exception {
        InternalTokenProperties properties = properties();
        InternalTokenService service = new DefaultInternalTokenService(new ObjectMapper(), properties);
        InternalTokenUserContext context = new InternalTokenUserContext(new ObjectMapper());
        String inbound = service.issue(new InternalTokenRequest(
                "base-organization", "user-1", "zhangsan", "base-middle",
                List.of("order:read"), List.of("admin"), 1, null, "trace-1",
                Map.of("tenant", "tenant-a")));
        InternalTokenClaims inboundClaims = service.verify(inbound, "base-middle");
        context.bind(inboundClaims);
        InternalTokenClientHttpRequestInterceptor interceptor =
                new InternalTokenClientHttpRequestInterceptor(
                        service,
                        new InternalTokenRequestFactory(context),
                        new HostInternalTokenTargetResolver(),
                        properties,
                        "base-middle");
        MockClientHttpRequest request = new MockClientHttpRequest(
                HttpMethod.GET, URI.create("http://base-order/api/orders"));
        request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer external");
        request.getHeaders().set(InternalTokenConstants.HEADER, inbound);
        request.getHeaders().set("x-client-token-user", "untrusted");

        interceptor.intercept(request, new byte[0],
                (outbound, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        String outbound = request.getHeaders().getFirst(InternalTokenConstants.HEADER);
        InternalTokenClaims outboundClaims = service.verify(outbound, "base-order");
        assertNotEquals(inboundClaims.tokenId(), outboundClaims.tokenId());
        assertEquals(inboundClaims.tokenId(), outboundClaims.parentTokenId());
        assertEquals(2, outboundClaims.hop());
        assertEquals("base-middle", outboundClaims.issuer());
        assertFalse(request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION));
        assertFalse(request.getHeaders().containsKey("x-client-token-user"));
    }

    @Test
    void shouldOnlyStripCredentialsWhenGloballyDisabled() throws Exception {
        InternalTokenProperties properties = properties();
        properties.setEnabled(false);
        InternalTokenUserContext context = new InternalTokenUserContext(new ObjectMapper());
        InternalTokenClientHttpRequestInterceptor interceptor =
                new InternalTokenClientHttpRequestInterceptor(
                        new DefaultInternalTokenService(new ObjectMapper(), properties),
                        new InternalTokenRequestFactory(context),
                        new HostInternalTokenTargetResolver(),
                        properties,
                        "base-middle");
        MockClientHttpRequest request = new MockClientHttpRequest(
                HttpMethod.GET, URI.create("http://base-order/api/orders"));
        request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer external");
        request.getHeaders().set(InternalTokenConstants.HEADER, "caller-token");

        interceptor.intercept(request, new byte[0],
                (outbound, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        assertFalse(request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION));
        assertFalse(request.getHeaders().containsKey(InternalTokenConstants.HEADER));
    }

    private static InternalTokenProperties properties() {
        InternalTokenProperties properties = new InternalTokenProperties();
        properties.setActiveKeyId("active");
        properties.setActiveKey(Base64.getEncoder().encodeToString(
                "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)));
        properties.setAllowedIssuers(Set.of("base-organization", "base-middle"));
        properties.setAllowedExtensionKeys(Set.of("tenant"));
        return properties;
    }
}
