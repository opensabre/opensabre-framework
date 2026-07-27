package io.github.opensabre.governance.dictionary;

/** 字典中心不可用或响应异常。 */
public class DictionaryUnavailableException extends RuntimeException {

    public DictionaryUnavailableException(String dictCode, Throwable cause) {
        super("Failed to load dictionary " + dictCode, cause);
    }
}
