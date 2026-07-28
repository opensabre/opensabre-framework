package io.github.opensabre.security.token;

import io.github.opensabre.common.core.util.UserContextHolder;
import io.github.opensabre.security.context.InternalTokenUserContext;
import io.github.opensabre.security.principal.InternalTokenPrincipal;
import io.github.opensabre.security.principal.InternalTokenPrincipalProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Creates a next-hop token request from the trusted current user context.
 */
public class InternalTokenRequestFactory {

    private final InternalTokenUserContext internalUserContext;
    private final InternalTokenPrincipalProvider principalProvider;

    public InternalTokenRequestFactory(InternalTokenUserContext internalUserContext) {
        this(internalUserContext, Optional::empty);
    }

    public InternalTokenRequestFactory(
            InternalTokenUserContext internalUserContext,
            InternalTokenPrincipalProvider principalProvider) {
        this.internalUserContext = internalUserContext;
        this.principalProvider = principalProvider;
    }

    /**
     * Creates a request for a concrete target service.
     *
     * @param issuer current application name
     * @param audience next-hop service name
     * @return next-hop token request
     */
    public InternalTokenRequest create(String issuer, String audience) {
        UserContextHolder holder = UserContextHolder.getInstance();
        InternalTokenClaims current = internalUserContext.currentClaims();
        InternalTokenPrincipal principal = current == null
                ? principalProvider.currentPrincipal().orElse(null)
                : null;
        String subject = current != null
                ? current.subject()
                : principal == null ? "service:" + issuer : principal.subject();
        String username = current != null
                ? current.username()
                : principal == null ? null : principal.username();
        List<String> scopes = current != null
                ? new ArrayList<>(holder.getScopes())
                : principal == null ? List.of() : principal.scopes();
        List<String> roles = current != null
                ? new ArrayList<>(holder.getRoles())
                : principal == null ? List.of() : principal.roles();
        int hop = current == null ? 1 : current.hop() + 1;
        String parentTokenId = current == null ? null : current.tokenId();
        Map<String, Object> extensions = current == null ? Map.of() : current.extensions();

        return new InternalTokenRequest(
                issuer,
                subject,
                username,
                audience,
                scopes,
                roles,
                hop,
                parentTokenId,
                holder.getValue("trace_id"),
                extensions);
    }

}
