package io.github.opensabre.security.webmvc;

import io.github.opensabre.security.token.InternalTokenClaims;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * Spring Security authentication backed by verified internal-token claims.
 */
public final class InternalTokenAuthenticationToken extends AbstractAuthenticationToken {

    private final InternalTokenClaims claims;

    public InternalTokenAuthenticationToken(
            InternalTokenClaims claims,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.claims = claims;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public InternalTokenClaims getPrincipal() {
        return claims;
    }

    @Override
    public String getName() {
        return StringUtils.hasText(claims.username())
                ? claims.username()
                : claims.subject();
    }
}
