package io.github.opensabre.security.key;

/**
 * Supplies the shared internal token key status currently visible to an application.
 */
@FunctionalInterface
public interface InternalTokenKeyStatusProvider {

    InternalTokenKeyStatus currentStatus();
}
