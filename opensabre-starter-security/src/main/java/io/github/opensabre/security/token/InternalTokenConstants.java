package io.github.opensabre.security.token;

/**
 * OpenSabre internal token protocol constants.
 */
public final class InternalTokenConstants {

    public static final String HEADER = "x-client-token";
    public static final String TYPE = "OS-INTERNAL";
    public static final String ALGORITHM = "HS256";

    private InternalTokenConstants() {
    }
}
