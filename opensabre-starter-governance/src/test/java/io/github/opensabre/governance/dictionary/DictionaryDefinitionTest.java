package io.github.opensabre.governance.dictionary;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DictionaryDefinitionTest {

    @Test
    void adaptsExistingEnumWithoutFrameworkInterface() {
        DictionaryDefinition definition = DictionaryDefinition.of(
                "sample_status", "样例状态", Status.values(), Status::code, Status::label);

        assertEquals("sample_status", definition.dictCode());
        assertEquals("1", definition.items().getFirst().value());
        assertEquals("正常", definition.items().getFirst().label());
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
}
