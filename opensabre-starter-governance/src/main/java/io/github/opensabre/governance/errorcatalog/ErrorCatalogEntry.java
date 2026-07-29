package io.github.opensabre.governance.errorcatalog;

import io.github.opensabre.common.core.exception.ErrorType;

/** A stable, centrally searchable description of one application error code. */
public record ErrorCatalogEntry(String code, String message, String module, Integer httpStatus,
                                boolean publicVisible, boolean deprecated, String description,
                                String owner, ErrorCatalogScope scope) {
    public ErrorCatalogEntry {
        if (isBlank(code) || isBlank(message)) throw new IllegalArgumentException("code and message are required");
        module = isBlank(module) ? "default" : module;
    }

    /**
     * Backward-compatible constructor for application-defined error codes.
     */
    public ErrorCatalogEntry(String code, String message, String module, Integer httpStatus,
                             boolean publicVisible, boolean deprecated, String description) {
        this(code, message, module, httpStatus, publicVisible, deprecated, description, null, null);
    }

    public static ErrorCatalogEntry from(ErrorType errorType, String module) {
        return new ErrorCatalogEntry(errorType.getCode(), errorType.getMesg(), module,
                null, true, false, null, null, null);
    }

    /**
     * Returns an entry with ownership defaults resolved for transport.
     */
    public ErrorCatalogEntry resolveOwnership(String application) {
        String resolvedOwner = isBlank(owner) ? application : owner;
        ErrorCatalogScope resolvedScope = scope == null ? ErrorCatalogScope.APPLICATION : scope;
        return new ErrorCatalogEntry(code, message, module, httpStatus, publicVisible,
                deprecated, description, resolvedOwner, resolvedScope);
    }

    private static boolean isBlank(String value) { return value == null || value.isBlank(); }
}
