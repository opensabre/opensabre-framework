package io.github.opensabre.governance.dictionary;

@OpenSabreDictionary(code = "scan_status", name = "扫描状态")
enum AnnotatedDictionary implements DictionaryEnum {
    ENABLED("enabled", "启用"),
    DISABLED("disabled", "停用");

    private final String value;
    private final String label;

    AnnotatedDictionary(String value, String label) {
        this.value = value;
        this.label = label;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String label() {
        return label;
    }
}

enum UnannotatedDictionary implements DictionaryEnum {
    VALUE;

    @Override
    public String value() {
        return "value";
    }

    @Override
    public String label() {
        return "值";
    }
}
