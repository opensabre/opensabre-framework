package io.github.opensabre.governance.dictionary;

import io.github.opensabre.common.core.exception.BaseException;
import io.github.opensabre.common.core.exception.SystemErrorType;

/** 字典中心不可用或响应异常。 */
public class DictionaryUnavailableException extends BaseException {

    public DictionaryUnavailableException(String dictCode, Throwable cause) {
        super(SystemErrorType.SYSTEM_BUSY, "Failed to load dictionary " + dictCode, cause);
    }
}
