package io.github.opensabre.governance.dictionary;

/** 字典项展示元数据。 */
public record DictionaryItem(String value, String label, Integer sort, String tagType, Integer status) {

    public boolean enabled() {
        return status == null || status == 1;
    }
}
