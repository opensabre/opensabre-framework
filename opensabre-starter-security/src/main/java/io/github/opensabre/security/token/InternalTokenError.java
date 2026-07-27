package io.github.opensabre.security.token;

/**
 * Stable internal token validation errors.
 */
public enum InternalTokenError {
    MISSING_TOKEN,
    MALFORMED_TOKEN,
    UNSUPPORTED_ALGORITHM,
    UNKNOWN_KEY,
    INVALID_SIGNATURE,
    INVALID_ISSUER,
    INVALID_AUDIENCE,
    INVALID_TIME,
    TOKEN_EXPIRED,
    TOKEN_TOO_LONG_LIVED,
    MAX_HOP_EXCEEDED,
    INVALID_EXTENSIONS,
    INVALID_CONFIGURATION
}
