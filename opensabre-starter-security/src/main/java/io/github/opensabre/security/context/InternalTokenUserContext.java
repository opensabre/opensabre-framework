package io.github.opensabre.security.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabre.common.core.util.UserContextHolder;
import io.github.opensabre.security.token.InternalTokenClaims;
import io.github.opensabre.security.token.InternalTokenError;
import io.github.opensabre.security.token.InternalTokenException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Binds verified internal token claims to the thread-local user context.
 */
public class InternalTokenUserContext {

    public static final String USER_ID = "user_id";
    public static final String ROLES = "roles";
    public static final String SCOPES = "scopes";
    public static final String TOKEN_ID = "token_id";
    public static final String TOKEN_ISSUER = "token_issuer";
    public static final String TOKEN_SOURCE = "token_source";
    public static final String TOKEN_TRACE_ID = "trace_id";
    public static final String EXTENSION_PREFIX = "ext.";

    private final ObjectMapper objectMapper;
    private final ThreadLocal<InternalTokenClaims> trustedClaims = new ThreadLocal<>();

    public InternalTokenUserContext(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Replaces the current thread context with trusted token data.
     *
     * @param claims verified claims
     */
    public void bind(InternalTokenClaims claims) {
        Map<String, String> context = new LinkedHashMap<>();
        context.put(USER_ID, claims.subject());
        putIfPresent(context, UserContextHolder.getInstance().KEY_USERNAME, claims.username());
        context.put(ROLES, String.join(",", claims.roles()));
        context.put(SCOPES, String.join(",", claims.scopes()));
        context.put(TOKEN_ID, claims.tokenId());
        context.put(TOKEN_ISSUER, claims.issuer());
        context.put(TOKEN_SOURCE, claims.source());
        putIfPresent(context, TOKEN_TRACE_ID, claims.traceId());
        claims.extensions().forEach(
                (key, value) -> context.put(EXTENSION_PREFIX + key, toContextValue(value)));
        UserContextHolder.getInstance().setContext(context);
        trustedClaims.set(claims);
    }

    /**
     * Returns the verified claims for the current request.
     *
     * @return verified claims, or {@code null} for an external/anonymous request
     */
    public InternalTokenClaims currentClaims() {
        return trustedClaims.get();
    }

    /**
     * Clears the current request context.
     */
    public void clear() {
        trustedClaims.remove();
        UserContextHolder.getInstance().clear();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new InternalTokenException(
                    InternalTokenError.INVALID_EXTENSIONS, "cannot bind token claims to user context", exception);
        }
    }

    private String toContextValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return toJson(value);
    }

    private static void putIfPresent(Map<String, String> context, String key, String value) {
        if (value != null && !value.isBlank()) {
            context.put(key, value);
        }
    }
}
