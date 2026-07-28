package io.github.opensabre.security.token;

/**
 * Raised when an internal token cannot be issued or trusted.
 */
public class InternalTokenException extends RuntimeException {

    private final InternalTokenError error;

    public InternalTokenException(InternalTokenError error, String message) {
        super(message);
        this.error = error;
    }

    public InternalTokenException(InternalTokenError error, String message, Throwable cause) {
        super(message, cause);
        this.error = error;
    }

    public InternalTokenError getError() {
        return error;
    }
}
