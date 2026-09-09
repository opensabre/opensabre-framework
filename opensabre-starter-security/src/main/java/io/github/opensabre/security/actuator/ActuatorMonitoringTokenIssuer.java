package io.github.opensabre.security.actuator;

import io.github.opensabre.security.config.InternalTokenProperties;
import io.github.opensabre.security.token.InternalTokenRequest;
import io.github.opensabre.security.token.InternalTokenService;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/** Issues least-privilege internal tokens for control-plane Actuator metric reads. */
public class ActuatorMonitoringTokenIssuer {

    private final InternalTokenService tokenService;
    private final InternalTokenProperties properties;
    private final String applicationName;

    public ActuatorMonitoringTokenIssuer(
            InternalTokenService tokenService,
            InternalTokenProperties properties,
            String applicationName) {
        this.tokenService = tokenService;
        this.properties = properties;
        this.applicationName = applicationName;
    }

    /** Issues a service token whose audience is the discovered target application. */
    public String issue(String audience) {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("OpenSabre internal token is required for Actuator monitoring");
        }
        if (!StringUtils.hasText(applicationName)) {
            throw new IllegalStateException("spring.application.name is required for Actuator monitoring");
        }
        if (!StringUtils.hasText(audience)) {
            throw new IllegalArgumentException("Actuator monitoring audience is required");
        }
        return tokenService.issue(new InternalTokenRequest(
                applicationName,
                "service:" + applicationName,
                null,
                audience,
                List.of(),
                List.of(),
                List.of(ActuatorMonitoringAccess.AUTHORITY),
                1,
                null,
                null,
                Map.of()));
    }
}
