package io.github.opensabre.security.key;

/**
 * Requests generation and activation of a new shared key.
 *
 * <p>The handler generates key material internally; management APIs must never accept
 * or return a shared secret.</p>
 */
public record InternalTokenKeyRotationRequest(
        long expectedConfigVersion,
        String newKeyId) {

    public InternalTokenKeyRotationRequest {
        if (expectedConfigVersion < 0) {
            throw new IllegalArgumentException("expectedConfigVersion must not be negative");
        }
        newKeyId = requireText(newKeyId, "newKeyId");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
