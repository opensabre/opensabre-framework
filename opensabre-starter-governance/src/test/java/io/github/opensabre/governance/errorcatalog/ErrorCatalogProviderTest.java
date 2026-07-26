package io.github.opensabre.governance.errorcatalog;

import io.github.opensabre.common.core.exception.SystemErrorType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorCatalogProviderTest {
    @Test
    void adaptsExistingErrorTypeWithoutChangingItsContract() {
        ErrorCatalogEntry entry = ErrorCatalogProvider.of("framework", SystemErrorType.values()).entries().iterator().next();
        assertEquals(SystemErrorType.SYSTEM_ERROR.getCode(), entry.code());
        assertEquals("framework", entry.module());
        assertEquals(SystemErrorType.SYSTEM_ERROR.getMesg(), entry.message());
    }
}
