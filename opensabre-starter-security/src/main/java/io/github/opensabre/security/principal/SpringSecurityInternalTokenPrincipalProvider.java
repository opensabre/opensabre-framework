package io.github.opensabre.security.principal;

import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Reads only an authenticated Spring Security principal.
 *
 * <p>Reflection keeps Spring Security optional for applications that only verify internal
 * tokens. Applications using another authentication stack can replace this provider.</p>
 */
public class SpringSecurityInternalTokenPrincipalProvider implements InternalTokenPrincipalProvider {

    private static final String SECURITY_CONTEXT_HOLDER =
            "org.springframework.security.core.context.SecurityContextHolder";
    private static final String ANONYMOUS_AUTHENTICATION =
            "org.springframework.security.authentication.AnonymousAuthenticationToken";

    @Override
    public Optional<InternalTokenPrincipal> currentPrincipal() {
        ClassLoader classLoader = getClass().getClassLoader();
        if (!ClassUtils.isPresent(SECURITY_CONTEXT_HOLDER, classLoader)) {
            return Optional.empty();
        }
        try {
            Class<?> holderClass = ClassUtils.forName(SECURITY_CONTEXT_HOLDER, classLoader);
            Object context = holderClass.getMethod("getContext").invoke(null);
            Object authentication = context.getClass().getMethod("getAuthentication").invoke(context);
            if (authentication == null
                    || !Boolean.TRUE.equals(invoke(authentication, "isAuthenticated"))
                    || isAnonymous(authentication, classLoader)) {
                return Optional.empty();
            }

            String authenticationName = text(invoke(authentication, "getName"));
            Object token = invokeIfPresent(authentication, "getToken");
            String subject = token == null ? authenticationName : text(invokeIfPresent(token, "getSubject"));
            String preferredUsername = token == null
                    ? null
                    : text(invokeIfPresent(token, "getClaimAsString", String.class, "preferred_username"));
            String username = hasText(preferredUsername) ? preferredUsername : authenticationName;
            if (!hasText(subject)) {
                return Optional.empty();
            }

            List<String> roles = new ArrayList<>();
            List<String> scopes = new ArrayList<>();
            List<String> directAuthorities = new ArrayList<>();
            Object authorities = invoke(authentication, "getAuthorities");
            if (authorities instanceof Collection<?> collection) {
                for (Object authority : collection) {
                    String value = text(invoke(authority, "getAuthority"));
                    if (value != null && value.startsWith("ROLE_") && value.length() > 5) {
                        roles.add(value.substring(5));
                    } else if (value != null && value.startsWith("SCOPE_") && value.length() > 6) {
                        scopes.add(value.substring(6));
                    } else if (hasText(value)) {
                        directAuthorities.add(value);
                    }
                }
            }
            return Optional.of(new InternalTokenPrincipal(
                    subject, username, roles, scopes, directAuthorities));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static boolean isAnonymous(Object authentication, ClassLoader classLoader)
            throws ReflectiveOperationException {
        if (!ClassUtils.isPresent(ANONYMOUS_AUTHENTICATION, classLoader)) {
            return false;
        }
        return ClassUtils.forName(ANONYMOUS_AUTHENTICATION, classLoader).isInstance(authentication);
    }

    private static Object invoke(Object target, String method) throws ReflectiveOperationException {
        return target.getClass().getMethod(method).invoke(target);
    }

    private static Object invokeIfPresent(Object target, String method)
            throws ReflectiveOperationException {
        try {
            return invoke(target, method);
        } catch (NoSuchMethodException exception) {
            return null;
        }
    }

    private static Object invokeIfPresent(
            Object target, String method, Class<?> parameterType, Object argument)
            throws ReflectiveOperationException {
        try {
            Method targetMethod = target.getClass().getMethod(method, parameterType);
            return targetMethod.invoke(target, argument);
        } catch (NoSuchMethodException exception) {
            return null;
        }
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
