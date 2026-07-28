package io.github.opensabre.governance.dictionary;

import java.util.Collection;

/** 应用显式提供的字典定义集合。 */
@FunctionalInterface
public interface DictionaryProvider {

    Collection<DictionaryDefinition> dictionaries();

    static DictionaryProvider of(DictionaryDefinition... definitions) {
        return () -> java.util.List.of(definitions);
    }
}
