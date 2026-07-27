package io.github.opensabre.security.key;

/**
 * Shared configuration backend SPI for internal token key changes.
 *
 * <p>Implementations must generate at least 256 bits of random key material, update the
 * shared configuration with optimistic locking, and never expose the generated secret.</p>
 */
public interface InternalTokenKeyRotationHandler {

    InternalTokenKeyStatus rotate(InternalTokenKeyRotationRequest request);

    InternalTokenKeyStatus retirePrevious(InternalTokenPreviousKeyRetirementRequest request);
}
