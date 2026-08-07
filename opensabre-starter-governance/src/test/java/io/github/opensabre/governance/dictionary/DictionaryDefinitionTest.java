package io.github.opensabre.governance.dictionary;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DictionaryDefinitionTest {

    @Test
    void adaptsExistingEnumWithoutFrameworkInterface() {
        DictionaryDefinition definition = DictionaryDefinition.of(
                "sample_status", "样例状态", Status.values(), Status::code, Status::label);

        assertEquals("sample_status", definition.dictCode());
        assertEquals("1", definition.items().get(0).value());
        assertEquals("正常", definition.items().get(0).label());
    }

    @Test
    void adaptsStandardDictionaryEnum() {
        DictionaryDefinition definition = DictionaryDefinition.of(
                "standard_status", "标准状态", StandardStatus.class);

        assertEquals("enabled", definition.items().get(0).value());
        assertEquals("启用", definition.items().get(0).label());
        assertEquals(10, definition.items().get(1).sort());
        assertEquals("S", definition.items().get(1).tagType());
    }

    private enum Status {
        ENABLED("1", "正常"),
        DISABLED("0", "禁用");

        private final String code;
        private final String label;

        Status(String code, String label) {
            this.code = code;
            this.label = label;
        }

        String code() {
            return code;
        }

        String label() {
            return label;
        }
    }

    private enum StandardStatus implements DictionaryEnum {
        ENABLED("enabled", "启用"),
        DISABLED("disabled", "停用");

        private final String value;
        private final String label;

        StandardStatus(String value, String label) {
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

        @Override
        public Integer sort() {
            return this == DISABLED ? 10 : 1;
        }

        @Override
        public String tagType() {
            return this == DISABLED ? "S" : "N";
        }
    }
}
