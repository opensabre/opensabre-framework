package io.github.opensabre.governance.errorcatalog;

import io.github.opensabre.common.core.exception.ErrorType;

/** A stable, centrally searchable description of one application error code. */
public record ErrorCatalogEntry(String code, String message, String module, Integer httpStatus,
                                boolean publicVisible, boolean deprecated, String description) {
    public ErrorCatalogEntry {
        if (isBlank(code) || isBlank(message)) throw new IllegalArgumentException("code and message are required");
        module = isBlank(module) ? "default" : module;
    }
    public static ErrorCatalogEntry from(ErrorType errorType, String module) {
        return new ErrorCatalogEntry(errorType.getCode(), errorType.getMesg(), module, null, true, false, null);
    }
    private static boolean isBlank(String value) { return value == null || value.isBlank(); }
}
