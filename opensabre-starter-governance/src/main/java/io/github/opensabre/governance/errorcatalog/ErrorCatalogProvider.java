package io.github.opensabre.governance.errorcatalog;

import io.github.opensabre.common.core.exception.ErrorType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** Application extension point for declaring business error codes at startup. */
@FunctionalInterface
public interface ErrorCatalogProvider {
    Collection<ErrorCatalogEntry> entries();
    static ErrorCatalogProvider of(String module, ErrorType[] errorTypes) {
        List<ErrorCatalogEntry> entries = Arrays.stream(errorTypes).map(type -> ErrorCatalogEntry.from(type, module)).toList();
        return () -> entries;
    }

    /**
     * Declares framework-owned error codes shared by every application.
     */
    static ErrorCatalogProvider common(String owner, String module, ErrorType[] errorTypes) {
        List<ErrorCatalogEntry> entries = Arrays.stream(errorTypes)
                .map(type -> new ErrorCatalogEntry(type.getCode(), type.getMesg(), module,
                        null, true, false, null, owner, ErrorCatalogScope.COMMON))
                .toList();
        return () -> entries;
    }
}
