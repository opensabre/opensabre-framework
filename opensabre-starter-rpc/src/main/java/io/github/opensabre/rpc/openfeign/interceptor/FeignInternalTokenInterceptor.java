package io.github.opensabre.rpc.openfeign.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Target;
import io.github.opensabre.security.config.InternalTokenProperties;
import io.github.opensabre.security.token.InternalTokenConstants;
import io.github.opensabre.security.token.InternalTokenRequestFactory;
import io.github.opensabre.security.token.InternalTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.Set;

/**
 * Re-signs an internal token for every Feign service hop.
 */
public class FeignInternalTokenInterceptor implements RequestInterceptor, Ordered {

    private static final Set<String> MANAGED_CREDENTIAL_HEADERS = Set.of(
            "authorization", "x-client-token", "x-client-token-user");

    private final InternalTokenService tokenService;
    private final InternalTokenRequestFactory requestFactory;
    private final InternalTokenProperties properties;
    private final String applicationName;

    public FeignInternalTokenInterceptor(
            InternalTokenService tokenService,
            InternalTokenRequestFactory requestFactory,
            InternalTokenProperties properties,
            @Value("${spring.application.name:}") String applicationName) {
        this.tokenService = tokenService;
        this.requestFactory = requestFactory;
        this.properties = properties;
        this.applicationName = applicationName;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (!properties.isEnabled()) {
            removeHeaders(template, Set.of("x-client-token", "x-client-token-user"));
            return;
        }
        // Remove every managed credential immediately before signing the next service hop.
        removeHeaders(template, MANAGED_CREDENTIAL_HEADERS);
        Target<?> target = template.feignTarget();
        if (target == null || !hasText(target.name())) {
            throw new IllegalStateException("Feign target service name is required for internal token signing");
        }
        if (!hasText(applicationName)) {
            throw new IllegalStateException("spring.application.name is required for internal token signing");
        }
        String token = tokenService.issue(requestFactory.create(applicationName, target.name()));
        template.header(InternalTokenConstants.HEADER, token);
    }

    private static void removeHeaders(RequestTemplate template, Set<String> headerNames) {
        new ArrayList<>(template.headers().keySet()).stream()
                .filter(name -> headerNames.stream().anyMatch(candidate -> candidate.equalsIgnoreCase(name)))
                .forEach(template::removeHeader);
    }

    /**
     * Runs after regular Feign interceptors so caller credentials are removed just before signing.
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
