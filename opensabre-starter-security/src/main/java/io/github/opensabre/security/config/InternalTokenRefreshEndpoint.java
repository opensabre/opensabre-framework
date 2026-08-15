package io.github.opensabre.security.config;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

/** Actuator endpoint exposing safe internal-token refresh metadata for rollout confirmation. */
@Endpoint(id = "internalTokenKeyStatus")
public class InternalTokenRefreshEndpoint {

    private final InternalTokenConfigurationRefresher refresher;

    public InternalTokenRefreshEndpoint(InternalTokenConfigurationRefresher refresher) {
        this.refresher = refresher;
    }

    @ReadOperation
    public InternalTokenRefreshStatus status() {
        return refresher.currentStatus();
    }
}
