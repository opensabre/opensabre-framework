package io.github.opensabre.security.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabre.common.core.util.UserContextHolder;
import io.github.opensabre.security.token.InternalTokenClaims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InternalTokenUserContextTest {

    @AfterEach
    void clearContext() {
        UserContextHolder.getInstance().clear();
    }

    @Test
    void shouldExposeTrustedUserInformation() {
        InternalTokenClaims claims = new InternalTokenClaims(
                "base-organization", "user-1", "zhangsan", "base-order", "jti-1",
                1, 1, 60, "base-organization", "base-order",
                List.of("order:read"), List.of("admin"), 1, null, "trace-1", 42,
                Map.of("tenant", "tenant-a"));

        new InternalTokenUserContext(new ObjectMapper()).bind(claims);

        UserContextHolder holder = UserContextHolder.getInstance();
        assertEquals("user-1", holder.getUserId());
        assertEquals("zhangsan", holder.getUsername());
        assertEquals(Set.of("admin"), holder.getRoles());
        assertEquals(Set.of("order:read"), holder.getScopes());
        assertEquals("tenant-a", holder.getValue("ext.tenant"));
    }
}
