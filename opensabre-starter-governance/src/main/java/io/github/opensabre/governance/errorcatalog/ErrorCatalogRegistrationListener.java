package io.github.opensabre.governance.errorcatalog;

import io.github.opensabre.boot.metadata.OpensabreVersion;
import io.github.opensabre.governance.client.SysadminGovernanceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Best-effort post-startup catalog registration; Sysadmin downtime never blocks the application. */
@Slf4j
@RequiredArgsConstructor
public class ErrorCatalogRegistrationListener {
    private final ObjectProvider<SysadminGovernanceClient> clientProvider;
    private final List<ErrorCatalogProvider> providers;
    private final Environment environment;
    @EventListener(ApplicationReadyEvent.class)
    public void register() { CompletableFuture.runAsync(this::registerSafely); }
    private void registerSafely() {
        try {
            Map<String, ErrorCatalogEntry> entriesByCode = new LinkedHashMap<>();
            for (ErrorCatalogProvider provider : providers) {
                Collection<ErrorCatalogEntry> entries = provider.entries();
                if (entries == null) continue;
                for (ErrorCatalogEntry entry : entries) {
                    ErrorCatalogEntry previous = entriesByCode.putIfAbsent(entry.code(), entry);
                    if (previous != null && !previous.equals(entry)) {
                        log.error("Skip error catalog registration: code {} conflicts locally", entry.code()); return;
                    }
                }
            }
            if (entriesByCode.isEmpty()) return;
            String application = environment.getProperty("spring.application.name", "unknown-application");
            String token = environment.getProperty("opensabre.governance.error-catalog.registration-token", "");
            List<ErrorCatalogEntry> resolvedEntries = entriesByCode.values().stream()
                    .map(entry -> entry.resolveOwnership(application))
                    .toList();
            clientProvider.getObject().registerErrorCatalog(new ErrorCatalogSnapshot(application,
                    OpensabreVersion.getVersion(), resolvedEntries), token);
            log.info("Registered {} error catalog entries for {}", entriesByCode.size(), application);
        } catch (Exception exception) { log.warn("Error catalog registration failed; startup is unaffected", exception); }
    }
}
