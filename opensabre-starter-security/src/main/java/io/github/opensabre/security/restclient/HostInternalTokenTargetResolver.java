package io.github.opensabre.security.restclient;

import java.net.URI;

/**
 * Uses the request URI host as the logical target service name.
 */
public class HostInternalTokenTargetResolver implements InternalTokenTargetResolver {

    @Override
    public String resolve(URI uri) {
        String host = uri == null ? null : uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalStateException(
                    "outbound request URI host is required for internal token signing");
        }
        return host;
    }
}
