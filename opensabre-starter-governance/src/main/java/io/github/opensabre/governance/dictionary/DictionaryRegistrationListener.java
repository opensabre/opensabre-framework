package io.github.opensabre.governance.dictionary;

import io.github.opensabre.governance.client.SysadminGovernanceClient;
import io.github.opensabre.governance.registration.GovernanceRegistrationCoordinator;
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

/**
 * 应用就绪后尽力注册字典，sysadmin 不可用不会阻塞应用启动。
 */
@Slf4j
@RequiredArgsConstructor
public class DictionaryRegistrationListener {

    private final ObjectProvider<SysadminGovernanceClient> clientProvider;
    private final List<DictionaryProvider> providers;
    private final Environment environment;
    private final GovernanceRegistrationCoordinator registrationCoordinator;

    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        registrationCoordinator.submit("dictionary", this::registerOnce);
    }

    private void registerOnce() {
            Map<String, DictionaryDefinition> definitions = new LinkedHashMap<>();
            for (DictionaryProvider provider : providers) {
                Collection<DictionaryDefinition> provided = provider.dictionaries();
                if (provided == null) {
                    continue;
                }
                for (DictionaryDefinition definition : provided) {
                    DictionaryDefinition previous = definitions.putIfAbsent(definition.dictCode(), definition);
                    if (previous != null && !previous.equals(definition)) {
                        throw new IllegalStateException(
                                "Dictionary code conflicts locally: " + definition.dictCode());
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
    }
}
