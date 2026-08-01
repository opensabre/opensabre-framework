package io.github.opensabre.security.token;

import java.util.List;
import java.util.Map;

/**
 * Information required to issue a token for the next service hop.
 */
public record InternalTokenRequest(
        String issuer,
        String subject,
        String username,
        String audience,
        List<String> scopes,
        List<String> roles,
        List<String> authorities,
        int hop,
        String parentTokenId,
        String traceId,
        Map<String, Object> extensions) {

    public InternalTokenRequest {
        scopes = scopes == null ? List.of() : List.copyOf(scopes);
        roles = roles == null ? List.of() : List.copyOf(roles);
        authorities = authorities == null ? List.of() : List.copyOf(authorities);
        extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
    }

    /**
     * Backward-compatible constructor for callers compiled against Framework 0.7.0.
     */
    public InternalTokenRequest(
            String issuer,
            String subject,
            String username,
            String audience,
            List<String> scopes,
            List<String> roles,
            int hop,
            String parentTokenId,
            String traceId,
            Map<String, Object> extensions) {
        this(issuer, subject, username, audience, scopes, roles, List.of(),
                hop, parentTokenId, traceId, extensions);
    }
}
