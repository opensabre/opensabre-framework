package io.github.opensabre.security.restclient;

import io.github.opensabre.security.config.InternalTokenProperties;
import io.github.opensabre.security.token.InternalTokenConstants;
import io.github.opensabre.security.token.InternalTokenRequestFactory;
import io.github.opensabre.security.token.InternalTokenService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Re-signs an internal token for every synchronous HTTP service hop.
 */
public class InternalTokenClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final String LEGACY_USER_HEADER = "x-client-token-user";

    private final InternalTokenService tokenService;
    private final InternalTokenRequestFactory requestFactory;
    private final InternalTokenTargetResolver targetResolver;
    private final InternalTokenProperties properties;
    private final String applicationName;

    public InternalTokenClientHttpRequestInterceptor(
            InternalTokenService tokenService,
            InternalTokenRequestFactory requestFactory,
            InternalTokenTargetResolver targetResolver,
            InternalTokenProperties properties,
            String applicationName) {
        this.tokenService = tokenService;
        this.requestFactory = requestFactory;
        this.targetResolver = targetResolver;
        this.properties = properties;
        this.applicationName = applicationName;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // A disabled integration must leave external HTTP requests untouched.
        if (!properties.isEnabled() || !properties.isRestClientEnabled()) {
            return execution.execute(request, body);
        }
        String target = targetResolver.resolve(request.getURI());
        if (!properties.getRestClientAllowedTargets().contains(target)) {
            return execution.execute(request, body);
        }

        HttpHeaders headers = request.getHeaders();
        headers.remove(HttpHeaders.AUTHORIZATION);
        headers.remove(InternalTokenConstants.HEADER);
        headers.remove(LEGACY_USER_HEADER);

        if (applicationName == null || applicationName.isBlank()) {
            throw new IllegalStateException(
                    "spring.application.name is required for internal token signing");
        }
        String token = tokenService.issue(requestFactory.create(applicationName, target));
        headers.set(InternalTokenConstants.HEADER, token);
        return execution.execute(request, body);
    }
}
