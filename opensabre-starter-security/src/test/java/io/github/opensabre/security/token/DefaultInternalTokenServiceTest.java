package io.github.opensabre.security.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabre.security.config.InternalTokenProperties;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultInternalTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-07-25T00:00:00Z");

    @Test
    void shouldIssueAndVerifyTrustedClaims() {
        InternalTokenProperties properties = properties("active", "0123456789abcdef0123456789abcdef");
        properties.setAllowedExtensionKeys(Set.of("tenant"));
        DefaultInternalTokenService service = service(properties, NOW);

        String token = service.issue(request("base-order", Map.of("tenant", "tenant-a")));
        InternalTokenClaims claims = service.verify(token, "base-order");

        assertEquals("base-organization", claims.issuer());
        assertEquals("user-1", claims.subject());
        assertEquals("zhangsan", claims.username());
        assertEquals(List.of("order:read"), claims.scopes());
        assertEquals(List.of("admin"), claims.roles());
        assertEquals("tenant-a", claims.extensions().get("tenant"));
        assertEquals(42, claims.keyConfigVersion());
    }

    @Test
    void shouldAcceptPreviousKeyDuringRotation() {
        InternalTokenProperties oldProperties = properties("old", "0123456789abcdef0123456789abcdef");
        String oldToken = service(oldProperties, NOW).issue(request("base-order", Map.of()));

        InternalTokenProperties rotated = properties("new", "abcdef0123456789abcdef0123456789");
        rotated.setPreviousKeyId("old");
        rotated.setPreviousKey(key("0123456789abcdef0123456789abcdef"));

        InternalTokenClaims claims = service(rotated, NOW.plusSeconds(30)).verify(oldToken, "base-order");
        assertEquals("user-1", claims.subject());
    }

    @Test
    void shouldRejectWrongAudience() {
        DefaultInternalTokenService service = service(
                properties("active", "0123456789abcdef0123456789abcdef"), NOW);
        String token = service.issue(request("base-order", Map.of()));

        InternalTokenException exception = assertThrows(
                InternalTokenException.class, () -> service.verify(token, "base-sysadmin"));

        assertEquals(InternalTokenError.INVALID_AUDIENCE, exception.getError());
    }

    @Test
    void shouldRejectExpiredToken() {
        InternalTokenProperties properties = properties("active", "0123456789abcdef0123456789abcdef");
        properties.setClockSkew(Duration.ZERO);
        String token = service(properties, NOW).issue(request("base-order", Map.of()));

        InternalTokenException exception = assertThrows(
                InternalTokenException.class,
                () -> service(properties, NOW.plusSeconds(61)).verify(token, "base-order"));

        assertEquals(InternalTokenError.TOKEN_EXPIRED, exception.getError());
    }

    @Test
    void shouldRejectUnknownExtension() {
        InternalTokenProperties properties = properties("active", "0123456789abcdef0123456789abcdef");
        properties.setAllowedExtensionKeys(Set.of("tenant"));

        InternalTokenException exception = assertThrows(
                InternalTokenException.class,
                () -> service(properties, NOW).issue(request("base-order", Map.of("mobile", "secret"))));

        assertEquals(InternalTokenError.INVALID_EXTENSIONS, exception.getError());
    }

    @Test
    void shouldRejectTamperedSignature() {
        DefaultInternalTokenService service = service(
                properties("active", "0123456789abcdef0123456789abcdef"), NOW);
        String token = service.issue(request("base-order", Map.of()));
        String tampered = token.substring(0, token.length() - 1)
                + (token.endsWith("a") ? "b" : "a");

        InternalTokenException exception = assertThrows(
                InternalTokenException.class, () -> service.verify(tampered, "base-order"));

        assertEquals(InternalTokenError.INVALID_SIGNATURE, exception.getError());
    }

    @Test
    void shouldRejectShortSharedKey() {
        InternalTokenProperties properties = properties("active", "short");

        InternalTokenException exception = assertThrows(
                InternalTokenException.class,
                () -> service(properties, NOW).issue(request("base-order", Map.of())));

        assertEquals(InternalTokenError.INVALID_CONFIGURATION, exception.getError());
    }

    private static DefaultInternalTokenService service(InternalTokenProperties properties, Instant instant) {
        return new DefaultInternalTokenService(
                new ObjectMapper(), properties, Clock.fixed(instant, ZoneOffset.UTC));
    }

    private static InternalTokenProperties properties(String keyId, String secret) {
        InternalTokenProperties properties = new InternalTokenProperties();
        properties.setActiveKeyId(keyId);
        properties.setActiveKey(key(secret));
        properties.setKeyConfigVersion(42);
        properties.setAllowedIssuers(Set.of("base-organization"));
        return properties;
    }

    private static String key(String secret) {
        return Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static InternalTokenRequest request(String audience, Map<String, Object> extensions) {
        return new InternalTokenRequest(
                "base-organization",
                "user-1",
                "zhangsan",
                audience,
                List.of("order:read"),
                List.of("admin"),
                1,
                "parent-token",
                "trace-1",
                extensions);
    }
}
