package io.github.opensabre.security.principal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpringSecurityInternalTokenPrincipalProviderTest {

    private final SpringSecurityInternalTokenPrincipalProvider provider =
            new SpringSecurityInternalTokenPrincipalProvider();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void separatesRolesScopesAndDirectAuthorities() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "operator",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ADMIN"),
                                new SimpleGrantedAuthority("ROLE_AUDITOR"),
                                new SimpleGrantedAuthority("SCOPE_keys.read"))));

        InternalTokenPrincipal principal = provider.currentPrincipal().orElseThrow();

        assertEquals("operator", principal.subject());
        assertEquals(List.of("AUDITOR"), principal.roles());
        assertEquals(List.of("keys.read"), principal.scopes());
        assertEquals(List.of("ADMIN"), principal.authorities());
    }
}
