package io.github.opensabre.rpc.openfeign.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestTemplate;
import feign.Target;
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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FeignInternalTokenInterceptorTest {

    @AfterEach
    void clearContext() {
        UserContextHolder.getInstance().clear();
    }

    @Test
    void shouldResignTokenForNextServiceHop() {
        InternalTokenProperties properties = properties();
        InternalTokenService service = new DefaultInternalTokenService(new ObjectMapper(), properties);
        InternalTokenUserContext context = new InternalTokenUserContext(new ObjectMapper());
        String inbound = service.issue(new InternalTokenRequest(
                "base-organization", "user-1", "zhangsan", "base-middle",
                List.of("order:read"), List.of("admin"), 1, null, "trace-1",
                Map.of("tenant", "tenant-a")));
        InternalTokenClaims inboundClaims = service.verify(inbound, "base-middle");
        context.bind(inboundClaims);
        FeignInternalTokenInterceptor interceptor = new FeignInternalTokenInterceptor(
                service, new InternalTokenRequestFactory(context), properties, "base-middle");
        RequestTemplate template = template("base-order");
        template.header(InternalTokenConstants.HEADER, inbound);

        interceptor.apply(template);

        String outbound = template.headers().get(InternalTokenConstants.HEADER).iterator().next();
        InternalTokenClaims outboundClaims = service.verify(outbound, "base-order");
        assertNotEquals(inboundClaims.tokenId(), outboundClaims.tokenId());
        assertEquals(inboundClaims.tokenId(), outboundClaims.parentTokenId());
        assertEquals(2, outboundClaims.hop());
        assertEquals("base-middle", outboundClaims.issuer());
        assertEquals("tenant-a", outboundClaims.extensions().get("tenant"));
    }

    @Test
    void shouldRemoveCallerTokenWhenGloballyDisabled() {
        InternalTokenProperties properties = properties();
        properties.setEnabled(false);
        InternalTokenUserContext context = new InternalTokenUserContext(new ObjectMapper());
        FeignInternalTokenInterceptor interceptor = new FeignInternalTokenInterceptor(
                new DefaultInternalTokenService(new ObjectMapper(), properties),
                new InternalTokenRequestFactory(context), properties, "base-middle");
        RequestTemplate template = template("base-order");
        template.header(InternalTokenConstants.HEADER, "caller-token");

        interceptor.apply(template);

        assertNull(template.headers().get(InternalTokenConstants.HEADER));
    }

    private static RequestTemplate template(String targetName) {
        RequestTemplate template = new RequestTemplate();
        template.feignTarget(new Target.HardCodedTarget<>(TestClient.class, targetName, "http://" + targetName));
        return template;
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

    private interface TestClient {
    }
}
