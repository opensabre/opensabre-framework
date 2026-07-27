package io.github.opensabre.governance.dictionary;

import java.util.List;

/** 应用启动时发送给 sysadmin 的字典快照。 */
public record DictionarySnapshot(String application, List<Definition> dictionaries) {
    public DictionarySnapshot {
        dictionaries = dictionaries == null ? List.of() : List.copyOf(dictionaries);
    }

    public static DictionarySnapshot from(String application, List<DictionaryDefinition> definitions) {
        return new DictionarySnapshot(application, definitions.stream().map(Definition::from).toList());
    }

    public record Definition(String dictCode, String dictName, List<Item> items) {
        private static Definition from(DictionaryDefinition definition) {
            return new Definition(definition.dictCode(), definition.dictName(),
                    definition.items().stream().map(Item::from).toList());
        }
    }

    public record Item(String value, String label, Integer sort, String tagType) {
        private static Item from(DictionaryItem item) {
            return new Item(item.value(), item.label(), item.sort(), item.tagType());
        }
    }
}
