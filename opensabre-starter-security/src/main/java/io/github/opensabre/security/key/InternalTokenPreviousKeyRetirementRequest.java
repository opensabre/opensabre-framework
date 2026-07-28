package io.github.opensabre.security.key;

/**
 * Requests removal of the previous key after the rotation grace period.
 */
public record InternalTokenPreviousKeyRetirementRequest(
        long expectedConfigVersion) {

    public InternalTokenPreviousKeyRetirementRequest {
        if (expectedConfigVersion < 0) {
            throw new IllegalArgumentException("expectedConfigVersion must not be negative");
        }
    }
}
