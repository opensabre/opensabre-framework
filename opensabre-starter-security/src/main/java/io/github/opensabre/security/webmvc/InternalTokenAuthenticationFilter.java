package io.github.opensabre.security.webmvc;

import io.github.opensabre.security.config.InternalTokenProperties;
import io.github.opensabre.security.context.InternalTokenUserContext;
import io.github.opensabre.security.token.InternalTokenClaims;
import io.github.opensabre.security.token.InternalTokenConstants;
import io.github.opensabre.security.token.InternalTokenException;
import io.github.opensabre.security.token.InternalTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Converts a verified internal token into a Spring Security authentication.
 *
 * <p>Applications using a {@code SecurityFilterChain} must add this filter before
 * Spring Security's bearer-token authentication filter. The filter is not registered as
 * a standalone Servlet filter because it must run inside Spring Security's context lifecycle.</p>
 */
public class InternalTokenAuthenticationFilter extends OncePerRequestFilter {

    private final InternalTokenService tokenService;
    private final InternalTokenUserContext userContext;
    private final InternalTokenProperties properties;
    private final String applicationName;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public InternalTokenAuthenticationFilter(
            InternalTokenService tokenService,
            InternalTokenUserContext userContext,
            InternalTokenProperties properties,
            String applicationName) {
        this.tokenService = tokenService;
        this.userContext = userContext;
        this.properties = properties;
        this.applicationName = applicationName;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(InternalTokenConstants.HEADER);
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!StringUtils.hasText(token)) {
            if (properties.isRequired() && !isExcluded(request.getRequestURI())) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "internal token is required");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }
        if (hasBearerToken(request)) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "external and internal tokens cannot be used together");
            return;
        }
        if (!StringUtils.hasText(applicationName)) {
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "spring.application.name is required");
            return;
        }

        SecurityContext previousContext = SecurityContextHolder.getContext();
        try {
            InternalTokenClaims claims = tokenService.verify(token, applicationName);
            userContext.bind(claims);
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(
                    new InternalTokenAuthenticationToken(claims, authorities(claims)));
            SecurityContextHolder.setContext(securityContext);
            filterChain.doFilter(request, response);
        } catch (InternalTokenException exception) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    exception.getError().name());
        } finally {
            userContext.clear();
            SecurityContextHolder.setContext(previousContext);
        }
    }

    private static boolean hasBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        return StringUtils.hasText(authorization)
                && authorization.regionMatches(true, 0, "Bearer ", 0, 7);
    }

    private boolean isExcluded(String requestUri) {
        return properties.getExcludedPaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }

    private static Set<GrantedAuthority> authorities(InternalTokenClaims claims) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        claims.roles().stream()
                .filter(StringUtils::hasText)
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        claims.scopes().stream()
                .filter(StringUtils::hasText)
                .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                .forEach(authorities::add);
        return authorities;
    }
}
