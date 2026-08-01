package io.github.opensabre.security.token;

import java.util.List;
import java.util.Map;

/**
 * Immutable trusted claims extracted from an OpenSabre internal token.
 *
 * @param issuer issuer service
 * @param subject user identifier
 * @param username display/login username
 * @param audience target service
 * @param tokenId unique token identifier
 * @param issuedAt issued epoch seconds
 * @param notBefore valid-from epoch seconds
 * @param expiresAt expiry epoch seconds
 * @param source source service
 * @param destination target service
 * @param scopes scope snapshot
 * @param roles role snapshot
 * @param authorities non-role authority snapshot
 * @param hop current service-call hop
 * @param parentTokenId previous-hop token identifier
 * @param traceId distributed trace identifier
 * @param keyConfigVersion shared key configuration version
 * @param extensions allow-listed extension claims
 */
public record InternalTokenClaims(
        String issuer,
        String subject,
        String username,
        String audience,
        String tokenId,
        long issuedAt,
        long notBefore,
        long expiresAt,
        String source,
        String destination,
        List<String> scopes,
        List<String> roles,
        List<String> authorities,
        int hop,
        String parentTokenId,
        String traceId,
        long keyConfigVersion,
        Map<String, Object> extensions) {

    public InternalTokenClaims {
        scopes = scopes == null ? List.of() : List.copyOf(scopes);
        roles = roles == null ? List.of() : List.copyOf(roles);
        authorities = authorities == null ? List.of() : List.copyOf(authorities);
        extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
    }

    /**
     * Backward-compatible constructor for 0.7.0 claim producers and tests.
     */
    public InternalTokenClaims(
            String issuer,
            String subject,
            String username,
            String audience,
            String tokenId,
            long issuedAt,
            long notBefore,
            long expiresAt,
            String source,
            String destination,
            List<String> scopes,
            List<String> roles,
            int hop,
            String parentTokenId,
            String traceId,
            long keyConfigVersion,
            Map<String, Object> extensions) {
        this(issuer, subject, username, audience, tokenId, issuedAt, notBefore, expiresAt,
                source, destination, scopes, roles, List.of(), hop, parentTokenId, traceId,
                keyConfigVersion, extensions);
    }
}
