package io.github.opensabre.security.actuator;

import io.github.opensabre.security.config.InternalTokenProperties;
import io.github.opensabre.security.token.InternalTokenClaims;
import io.github.opensabre.security.token.InternalTokenRequest;
import io.github.opensabre.security.token.InternalTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ActuatorMonitoringWebFilterTest {

    @Test
    void allowsAuthorizedInternalToken() {
        AtomicBoolean invoked = new AtomicBoolean();
        var exchange = exchange("signed-token");

        filter(List.of(ActuatorMonitoringAccess.AUTHORITY))
                .filter(exchange, current -> {
                    invoked.set(true);
                    return Mono.empty();
                }).block();

        assertThat(invoked).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void rejectsMissingTokenAndMissingAuthority() {
        var missing = exchange(null);
        filter(List.of(ActuatorMonitoringAccess.AUTHORITY))
                .filter(missing, current -> Mono.empty()).block();
        assertThat(missing.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        var forbidden = exchange("signed-token");
        filter(List.of()).filter(forbidden, current -> Mono.empty()).block();
        assertThat(forbidden.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private static MockServerWebExchange exchange(String token) {
        var request = MockServerHttpRequest.get(
                "/actuator/metrics/process.cpu.usage");
        if (token != null) request.header("x-client-token", token);
        return MockServerWebExchange.from(request.build());
    }

    private static ActuatorMonitoringWebFilter filter(List<String> authorities) {
        InternalTokenProperties properties = new InternalTokenProperties();
        properties.setEnabled(true);
        InternalTokenService service = new InternalTokenService() {
            @Override
            public String issue(InternalTokenRequest request) {
                throw new UnsupportedOperationException();
            }

            @Override
            public InternalTokenClaims verify(String token, String audience) {
                return new InternalTokenClaims(
                        "base-gateway-admin", "service:base-gateway-admin", null,
                        audience, "token-1", 1, 1, 60,
                        "base-gateway-admin", audience, List.of(), List.of(), authorities,
                        1, null, null, 1, Map.of());
            }
        };
        return new ActuatorMonitoringWebFilter(service, properties, "base-gateway");
    }
}
