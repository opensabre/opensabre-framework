package io.github.opensabre.governance.dictionary;

import java.util.List;
import java.util.Optional;

/** 后端读取字典的统一入口。 */
public interface DictionaryService {

    List<DictionaryItem> items(String dictCode);

    /** 包含停用项，用于历史数据回显。 */
    Optional<String> labelOf(String dictCode, Object value);

    boolean contains(String dictCode, Object value);

    void refresh(String dictCode);
}
