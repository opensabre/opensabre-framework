package io.github.opensabre.security.principal;

import java.util.Optional;

/**
 * Supplies a trusted principal for the first internal service hop.
 */
@FunctionalInterface
public interface InternalTokenPrincipalProvider {

    Optional<InternalTokenPrincipal> currentPrincipal();
}
