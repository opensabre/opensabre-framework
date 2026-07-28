package io.github.opensabre.governance.dictionary;

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

/**
 * 应用就绪后尽力注册字典，sysadmin 不可用不会阻塞应用启动。
 */
@Slf4j
@RequiredArgsConstructor
public class DictionaryRegistrationListener {

    private final ObjectProvider<SysadminGovernanceClient> clientProvider;
    private final List<DictionaryProvider> providers;
    private final Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        CompletableFuture.runAsync(this::registerSafely);
    }

    private void registerSafely() {
        try {
            Map<String, DictionaryDefinition> definitions = new LinkedHashMap<>();
            for (DictionaryProvider provider : providers) {
                Collection<DictionaryDefinition> provided = provider.dictionaries();
                if (provided == null) {
                    continue;
                }
                for (DictionaryDefinition definition : provided) {
                    DictionaryDefinition previous = definitions.putIfAbsent(definition.dictCode(), definition);
                    if (previous != null && !previous.equals(definition)) {
                        log.error("Skip dictionary registration: code {} conflicts locally", definition.dictCode());
                        return;
                    }
                }
            }
            if (definitions.isEmpty()) {
                return;
            }
            String application = environment.getProperty("spring.application.name", "unknown-application");
            String token = environment.getProperty("opensabre.governance.dictionary.registration-token", "");
            clientProvider.getObject().registerDictionaries(
                    DictionarySnapshot.from(application, List.copyOf(definitions.values())), token);
            log.info("Registered {} dictionaries for {}", definitions.size(), application);
        } catch (Exception exception) {
            log.warn("Dictionary registration failed; startup is unaffected", exception);
        }
    }
}
