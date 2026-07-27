package io.github.opensabre.rpc.openfeign.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Target;
import io.github.opensabre.security.config.InternalTokenProperties;
import io.github.opensabre.security.token.InternalTokenConstants;
import io.github.opensabre.security.token.InternalTokenRequestFactory;
import io.github.opensabre.security.token.InternalTokenService;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;

/**
 * Re-signs an internal token for every Feign service hop.
 */
public class FeignInternalTokenInterceptor implements RequestInterceptor {

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
        // Never forward a caller-provided token unchanged.
        new ArrayList<>(template.headers().keySet()).stream()
                .filter(name -> InternalTokenConstants.HEADER.equalsIgnoreCase(name))
                .forEach(template::removeHeader);
        if (!properties.isEnabled()) {
            return;
        }
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

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
