package io.github.opensabre.common.core.exception;

public interface ErrorType {
    /**
     * 返回code
     *
     * @return 错误code
     */
    String getCode();

    /**
     * 返回mesg
     *
     * @return 错误信息
     */
    String getMesg();
}
