package io.github.opensabre.security.principal;

import java.util.List;

/**
 * Trusted external authentication data used to issue the first internal token.
 */
public record InternalTokenPrincipal(
        String subject,
        String username,
        List<String> roles,
        List<String> scopes,
        List<String> authorities) {

    public InternalTokenPrincipal {
        roles = roles == null ? List.of() : List.copyOf(roles);
        scopes = scopes == null ? List.of() : List.copyOf(scopes);
        authorities = authorities == null ? List.of() : List.copyOf(authorities);
    }

    /**
     * Backward-compatible constructor for callers without direct authorities.
     */
    public InternalTokenPrincipal(
            String subject,
            String username,
            List<String> roles,
            List<String> scopes) {
        this(subject, username, roles, scopes, List.of());
    }
}
