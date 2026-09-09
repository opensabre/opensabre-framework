package io.github.opensabre.security.actuator;

import io.github.opensabre.security.config.InternalTokenProperties;
import io.github.opensabre.security.token.InternalTokenClaims;
import io.github.opensabre.security.token.InternalTokenRequest;
import io.github.opensabre.security.token.InternalTokenService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActuatorMonitoringTokenIssuerTest {

    @Test
    void issuesLeastPrivilegeServiceTokenForTargetApplication() {
        InternalTokenProperties properties = new InternalTokenProperties();
        properties.setEnabled(true);
        AtomicReference<InternalTokenRequest> request = new AtomicReference<>();
        InternalTokenService service = capturingService(request);

        String token = new ActuatorMonitoringTokenIssuer(
                service, properties, "base-gateway-admin").issue("iqc-platform");

        assertThat(token).isEqualTo("signed-token");
        assertThat(request.get().issuer()).isEqualTo("base-gateway-admin");
        assertThat(request.get().subject()).isEqualTo("service:base-gateway-admin");
        assertThat(request.get().audience()).isEqualTo("iqc-platform");
        assertThat(request.get().authorities())
                .containsExactly(ActuatorMonitoringAccess.AUTHORITY);
        assertThat(request.get().roles()).isEmpty();
        assertThat(request.get().scopes()).isEmpty();
    }

    @Test
    void failsClosedWhenInternalTokensAreDisabled() {
        InternalTokenProperties properties = new InternalTokenProperties();

        assertThatThrownBy(() -> new ActuatorMonitoringTokenIssuer(
                capturingService(new AtomicReference<>()), properties,
                "base-gateway-admin").issue("iqc-platform"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("internal token is required");
    }

    private static InternalTokenService capturingService(
            AtomicReference<InternalTokenRequest> request) {
        return new InternalTokenService() {
            @Override
            public String issue(InternalTokenRequest value) {
                request.set(value);
                return "signed-token";
            }

            @Override
            public InternalTokenClaims verify(String token, String expectedAudience) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
