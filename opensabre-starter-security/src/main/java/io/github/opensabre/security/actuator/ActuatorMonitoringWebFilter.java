package io.github.opensabre.security.actuator;

import io.github.opensabre.security.config.InternalTokenProperties;
import io.github.opensabre.security.token.InternalTokenConstants;
import io.github.opensabre.security.token.InternalTokenException;
import io.github.opensabre.security.token.InternalTokenService;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/** Enforces the shared internal-token contract on reactive Actuator metric endpoints. */
public class ActuatorMonitoringWebFilter implements WebFilter, Ordered {

    private final InternalTokenService tokenService;
    private final InternalTokenProperties properties;
    private final String applicationName;

    public ActuatorMonitoringWebFilter(
            InternalTokenService tokenService,
            InternalTokenProperties properties,
            String applicationName) {
        this.tokenService = tokenService;
        this.properties = properties;
        this.applicationName = applicationName;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        if (!ActuatorMonitoringAccess.metricPaths().contains(path)) {
            return chain.filter(exchange);
        }
        String token = exchange.getRequest().getHeaders().getFirst(InternalTokenConstants.HEADER);
        if (!properties.isEnabled() || !StringUtils.hasText(token)
                || !StringUtils.hasText(applicationName)) {
            return reject(exchange, HttpStatus.UNAUTHORIZED);
        }
        try {
            var claims = tokenService.verify(token, applicationName);
            if (!claims.authorities().contains(ActuatorMonitoringAccess.AUTHORITY)) {
                return reject(exchange, HttpStatus.FORBIDDEN);
            }
            return chain.filter(exchange);
        } catch (InternalTokenException exception) {
            return reject(exchange, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }

    private static Mono<Void> reject(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
