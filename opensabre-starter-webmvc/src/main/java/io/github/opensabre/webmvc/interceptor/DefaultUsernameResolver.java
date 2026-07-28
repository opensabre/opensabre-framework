package io.github.opensabre.webmvc.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 默认用户名解析器。
 * 使用 OAuth2 Resource Server 时从已验证的 Spring Security 上下文读取用户名，
 * 未使用时仅从外部 Authorization JWT subject 读取。
 */
public class DefaultUsernameResolver implements UsernameResolver {

    private static final String SECURITY_CONTEXT_HOLDER_CLASS =
            "org.springframework.security.core.context.SecurityContextHolder";
    private static final String JWT_AUTHENTICATION_TOKEN_CLASS =
            "org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String resolve(HttpServletRequest request) {
        if (isResourceServerAvailable()) {
            return getUsernameFromSecurityContext();
        }
        return getUsernameFromJwt(request);
    }

    private boolean isResourceServerAvailable() {
        return ClassUtils.isPresent(JWT_AUTHENTICATION_TOKEN_CLASS, getClass().getClassLoader());
    }

    private String getUsernameFromSecurityContext() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            Class<?> securityContextHolderClass = ClassUtils.forName(SECURITY_CONTEXT_HOLDER_CLASS, classLoader);
            Class<?> jwtAuthenticationTokenClass = ClassUtils.forName(JWT_AUTHENTICATION_TOKEN_CLASS, classLoader);
            Object securityContext = securityContextHolderClass.getMethod("getContext").invoke(null);
            Object authentication = securityContext.getClass().getMethod("getAuthentication").invoke(securityContext);
            if (!jwtAuthenticationTokenClass.isInstance(authentication)) {
                return StringUtils.EMPTY;
            }
            Object jwt = jwtAuthenticationTokenClass.getMethod("getToken").invoke(authentication);
            return (String) jwt.getClass().getMethod("getSubject").invoke(jwt);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException | ClassCastException e) {
            return StringUtils.EMPTY;
        }
    }

    private String getUsernameFromJwt(HttpServletRequest request) {
        String token = getJwtToken(request);
        if (StringUtils.isBlank(token)) {
            return StringUtils.EMPTY;
        }
        String[] tokenParts = token.split("\\.");
        if (tokenParts.length < 2) {
            return StringUtils.EMPTY;
        }
        try {
            String payload = new String(Base64.getUrlDecoder().decode(tokenParts[1]), StandardCharsets.UTF_8);
            JsonNode claims = objectMapper.readTree(payload);
            return claims.path("sub").asText(StringUtils.EMPTY);
        } catch (IllegalArgumentException | java.io.IOException e) {
            return StringUtils.EMPTY;
        }
    }

    private String getJwtToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        return StringUtils.removeStartIgnoreCase(StringUtils.trimToEmpty(authorization), "Bearer ");
    }
}
