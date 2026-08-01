package io.github.opensabre.security.webmvc;

import io.github.opensabre.security.config.InternalTokenProperties;
import io.github.opensabre.security.context.InternalTokenUserContext;
import io.github.opensabre.security.token.InternalTokenConstants;
import io.github.opensabre.security.token.InternalTokenError;
import io.github.opensabre.security.token.InternalTokenException;
import io.github.opensabre.security.token.InternalTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Verifies an inbound internal token before a servlet controller is invoked.
 */
public class InternalTokenMvcInterceptor implements HandlerInterceptor {

    private static final String BOUND_ATTRIBUTE =
            InternalTokenMvcInterceptor.class.getName() + ".BOUND";

    private final InternalTokenService tokenService;
    private final InternalTokenUserContext userContext;
    private final InternalTokenProperties properties;
    private final String applicationName;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public InternalTokenMvcInterceptor(
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
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!properties.isEnabled()) {
            return true;
        }
        if (userContext.currentClaims() != null) {
            return true;
        }
        String token = request.getHeader(InternalTokenConstants.HEADER);
        if (!StringUtils.hasText(token)) {
            if (properties.isRequired() && !isExcluded(request.getRequestURI())) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "internal token is required");
                return false;
            }
            return true;
        }
        if (hasBearerToken(request)) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    InternalTokenError.AMBIGUOUS_CREDENTIALS.name());
            return false;
        }
        if (!StringUtils.hasText(applicationName)) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "spring.application.name is required");
            return false;
        }
        try {
            userContext.bind(tokenService.verify(token, applicationName));
            request.setAttribute(BOUND_ATTRIBUTE, Boolean.TRUE);
            return true;
        } catch (InternalTokenException exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getError().name());
            return false;
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

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            @Nullable Exception exception) {
        if (Boolean.TRUE.equals(request.getAttribute(BOUND_ATTRIBUTE))) {
            userContext.clear();
        }
    }
}
