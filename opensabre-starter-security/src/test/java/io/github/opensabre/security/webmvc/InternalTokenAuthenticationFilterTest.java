package io.github.opensabre.security.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabre.common.core.util.UserContextHolder;
import io.github.opensabre.security.config.InternalTokenProperties;
import io.github.opensabre.security.context.InternalTokenUserContext;
import io.github.opensabre.security.token.InternalTokenClaims;
import io.github.opensabre.security.token.InternalTokenConstants;
import io.github.opensabre.security.token.InternalTokenError;
import io.github.opensabre.security.token.InternalTokenException;
import io.github.opensabre.security.token.InternalTokenRequest;
import io.github.opensabre.security.token.InternalTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class InternalTokenAuthenticationFilterTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
        UserContextHolder.getInstance().clear();
    }

    @Test
    void authenticatesVerifiedInternalTokenAndClearsUserContext() throws Exception {
        InternalTokenClaims claims = claims();
        InternalTokenAuthenticationFilter filter = filter(verifier((token, audience) -> claims));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(InternalTokenConstants.HEADER, "signed-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Authentication> observed = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            observed.set(SecurityContextHolder.getContext().getAuthentication());
            assertThat(UserContextHolder.getInstance().getUsername()).isEqualTo("admin");
        });

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(observed.get()).isNotNull();
        assertThat(observed.get().getName()).isEqualTo("admin");
        assertThat(observed.get().getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ADMIN", "SCOPE_internal-token:read");
        assertThat(UserContextHolder.getInstance().getContext()).isEmpty();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void rejectsInvalidInternalToken() throws Exception {
        InternalTokenAuthenticationFilter filter = filter(verifier((token, audience) -> {
            throw new InternalTokenException(
                    InternalTokenError.INVALID_SIGNATURE, "invalid signature");
        }));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(InternalTokenConstants.HEADER, "forged");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getErrorMessage()).isEqualTo("INVALID_SIGNATURE");
    }

    @Test
    void rejectsAmbiguousExternalAndInternalCredentials() throws Exception {
        InternalTokenAuthenticationFilter filter =
                filter(verifier((token, audience) -> claims()));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(InternalTokenConstants.HEADER, "signed-token");
        request.addHeader("Authorization", "Bearer external-jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void rejectsMissingTokenWhenRequiredBeforeAuthorization() throws Exception {
        InternalTokenProperties properties = new InternalTokenProperties();
        properties.setEnabled(true);
        properties.setRequired(true);
        InternalTokenAuthenticationFilter filter = new InternalTokenAuthenticationFilter(
                verifier((token, audience) -> claims()),
                new InternalTokenUserContext(new ObjectMapper()),
                properties,
                "sample-provider");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/user/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getErrorMessage()).isEqualTo("internal token is required");
    }

    private static InternalTokenAuthenticationFilter filter(InternalTokenService tokenService) {
        InternalTokenProperties properties = new InternalTokenProperties();
        properties.setEnabled(true);
        return new InternalTokenAuthenticationFilter(
                tokenService,
                new InternalTokenUserContext(new ObjectMapper()),
                properties,
                "base-sysadmin");
    }

    private static InternalTokenService verifier(TokenVerifier verifier) {
        return new InternalTokenService() {
            @Override
            public String issue(InternalTokenRequest request) {
                throw new UnsupportedOperationException();
            }

            @Override
            public InternalTokenClaims verify(String token, String expectedAudience) {
                return verifier.verify(token, expectedAudience);
            }
        };
    }

    @FunctionalInterface
    private interface TokenVerifier {
        InternalTokenClaims verify(String token, String expectedAudience);
    }

    private static InternalTokenClaims claims() {
        return new InternalTokenClaims(
                "base-order", "user-1", "admin", "base-sysadmin", "jti-1",
                1, 1, 60, "base-order", "base-sysadmin",
                List.of("internal-token:read"), List.of("ADMIN"), 1, null, "trace-1", 3,
                Map.of("tenant", "default"));
    }
}
