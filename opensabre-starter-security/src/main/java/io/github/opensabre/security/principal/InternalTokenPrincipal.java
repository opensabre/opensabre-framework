package io.github.opensabre.security.principal;

import java.util.List;

/**
 * Trusted external authentication data used to issue the first internal token.
 */
public record InternalTokenPrincipal(
        String subject,
        String username,
        List<String> roles,
        List<String> scopes) {

    public InternalTokenPrincipal {
        roles = roles == null ? List.of() : List.copyOf(roles);
        scopes = scopes == null ? List.of() : List.copyOf(scopes);
    }
}
