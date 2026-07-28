package io.github.opensabre.governance.dictionary;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

/** 应用声明的枚举字典定义。 */
public record DictionaryDefinition(String dictCode, String dictName, List<DictionaryItem> items) {

    public DictionaryDefinition {
        items = items == null ? List.of() : List.copyOf(items);
    }

    /**
     * 将已有枚举转换为字典定义，无需枚举实现框架接口。
     */
    public static <E> DictionaryDefinition of(String code, String name, E[] values,
                                               Function<E, ?> valueMapper,
                                               Function<E, String> labelMapper) {
        List<DictionaryItem> items = IntStream.range(0, values.length)
                .mapToObj(index -> new DictionaryItem(
                        String.valueOf(valueMapper.apply(values[index])),
                        labelMapper.apply(values[index]), index + 1, "N", 1))
                .toList();
        return new DictionaryDefinition(code, name, items);
    }
}
