package io.github.opensabre.security.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabre.common.core.util.UserContextHolder;
import io.github.opensabre.security.context.InternalTokenUserContext;
import io.github.opensabre.security.principal.InternalTokenPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InternalTokenRequestFactoryTest {

    @AfterEach
    void clearContext() {
        UserContextHolder.getInstance().clear();
    }

    @Test
    void shouldUseVerifiedExternalPrincipalForFirstHop() {
        InternalTokenUserContext context = new InternalTokenUserContext(new ObjectMapper());
        InternalTokenRequestFactory factory = new InternalTokenRequestFactory(
                context,
                () -> Optional.of(new InternalTokenPrincipal(
                        "user-1", "zhangsan", List.of("admin"), List.of("order:read"))));

        InternalTokenRequest request = factory.create("base-organization", "base-order");

        assertEquals("user-1", request.subject());
        assertEquals("zhangsan", request.username());
        assertEquals(List.of("admin"), request.roles());
        assertEquals(List.of("order:read"), request.scopes());
        assertEquals(1, request.hop());
        assertNull(request.parentTokenId());
    }

    @Test
    void shouldIgnoreUntrustedUserContextWithoutVerifiedPrincipal() {
        UserContextHolder.getInstance().setContext(Map.of(
                "user_name", "attacker",
                "user_id", "forged-user",
                "roles", "admin",
                "scopes", "all"));
        InternalTokenRequestFactory factory = new InternalTokenRequestFactory(
                new InternalTokenUserContext(new ObjectMapper()),
                Optional::empty);

        InternalTokenRequest request = factory.create("base-organization", "base-order");

        assertEquals("service:base-organization", request.subject());
        assertNull(request.username());
        assertEquals(List.of(), request.roles());
        assertEquals(List.of(), request.scopes());
    }

    @Test
    void shouldPreferVerifiedInternalClaimsOverExternalPrincipal() {
        InternalTokenUserContext context = new InternalTokenUserContext(new ObjectMapper());
        context.bind(new InternalTokenClaims(
                "base-middle",
                "internal-user",
                "internal-name",
                "base-organization",
                "token-1",
                1,
                1,
                61,
                "base-middle",
                "base-organization",
                List.of("internal:read"),
                List.of("operator"),
                2,
                null,
                "trace-1",
                3,
                Map.of()));
        InternalTokenRequestFactory factory = new InternalTokenRequestFactory(
                context,
                () -> Optional.of(new InternalTokenPrincipal(
                        "external-user", "external-name", List.of("admin"), List.of("all"))));

        InternalTokenRequest request = factory.create("base-organization", "base-order");

        assertEquals("internal-user", request.subject());
        assertEquals("internal-name", request.username());
        assertEquals(List.of("operator"), request.roles());
        assertEquals(List.of("internal:read"), request.scopes());
        assertEquals(3, request.hop());
        assertEquals("token-1", request.parentTokenId());
    }
}
