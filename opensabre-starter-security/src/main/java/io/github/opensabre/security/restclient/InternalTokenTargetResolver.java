package io.github.opensabre.security.restclient;

import java.net.URI;

/**
 * Resolves the logical target service for an outbound HTTP request.
 */
@FunctionalInterface
public interface InternalTokenTargetResolver {

    /**
     * Resolves the service name used as the internal token audience.
     *
     * @param uri outbound request URI
     * @return logical target service name
     */
    String resolve(URI uri);
}
