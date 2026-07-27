package io.github.opensabre.security.key;

import java.time.Instant;

/**
 * Safe-to-display shared key metadata. Secret key material is deliberately excluded.
 */
public record InternalTokenKeyStatus(
        boolean enabled,
        long configVersion,
        String activeKeyId,
        boolean activeKeyConfigured,
        String previousKeyId,
        boolean previousKeyConfigured,
        Instant activeKeyActivatedAt,
        Instant previousKeyRetireAfter) {
}
