package io.github.opensabre.security.config;

import java.time.Instant;

/** Safe runtime metadata for the last internal-token configuration refresh. */
public record InternalTokenRefreshStatus(
        long configVersion,
        String activeKeyId,
        Instant refreshedAt,
        boolean successful,
        String message) {
}
