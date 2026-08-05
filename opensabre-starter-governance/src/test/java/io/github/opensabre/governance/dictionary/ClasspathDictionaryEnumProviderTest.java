package io.github.opensabre.governance.dictionary;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClasspathDictionaryEnumProviderTest {

    @Test
    void discoversAnnotatedStandardEnumInConfiguredPackage() {
        ClasspathDictionaryEnumProvider provider = new ClasspathDictionaryEnumProvider(
                new DefaultResourceLoader(), List.of("io.github.opensabre.governance.dictionary"));

        List<DictionaryDefinition> definitions = provider.dictionaries().stream().toList();

        assertEquals(1, definitions.size());
        DictionaryDefinition definition = definitions.stream()
                .filter(item -> item.dictCode().equals("scan_status"))
                .findFirst()
                .orElseThrow();
        assertEquals("扫描状态", definition.dictName());
        assertEquals("enabled", definition.items().get(0).value());
        assertEquals(2, definition.items().size());
    }

    @Test
    void doesNotScanWhenNoPackageIsConfigured() {
        ClasspathDictionaryEnumProvider provider = new ClasspathDictionaryEnumProvider(
                new DefaultResourceLoader(), List.of());

        assertTrue(provider.dictionaries().isEmpty());
    }
}
