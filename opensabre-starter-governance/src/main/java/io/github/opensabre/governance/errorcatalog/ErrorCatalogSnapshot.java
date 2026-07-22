package io.github.opensabre.governance.errorcatalog;

import java.util.List;

/** Complete error-code declaration sent by one application after it is ready. */
public record ErrorCatalogSnapshot(String application, String version, List<ErrorCatalogEntry> entries) {
    public ErrorCatalogSnapshot {
        if (application == null || application.isBlank()) throw new IllegalArgumentException("application is required");
        entries = entries == null ? List.of() : List.copyOf(entries);
    }
}
