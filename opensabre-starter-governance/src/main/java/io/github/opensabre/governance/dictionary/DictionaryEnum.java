package io.github.opensabre.governance.dictionary;

/**
 * OpenSabre 标准字典枚举契约。
 *
 * <p>实现该接口并使用 {@link OpenSabreDictionary} 标注后，Framework 会在应用启动时自动发现并上报。</p>
 */
public interface DictionaryEnum {

    /**
     * 字典值，保存到业务数据中的稳定值。
     *
     * @return 字典值
     */
    String value();

    /**
     * 字典展示标签。
     *
     * @return 展示标签
     */
    String label();

    /**
     * 可选展示顺序；返回 {@code null} 时使用枚举声明顺序。
     *
     * @return 展示顺序
     */
    default Integer sort() {
        return null;
    }

    /**
     * Element Plus 等前端使用的标签类型编码。
     *
     * @return 标签类型编码
     */
    default String tagType() {
        return "N";
    }
}
