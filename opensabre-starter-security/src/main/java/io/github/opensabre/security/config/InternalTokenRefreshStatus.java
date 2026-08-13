package io.github.opensabre.security.config;

import java.time.Instant;

public record InternalTokenRefreshStatus(long configVersion, String activeKeyId, Instant refreshedAt,
                                         boolean successful, String message) {
}
