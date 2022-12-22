package io.github.opensabre.common.core.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaseExceptionTest {
    @Test
    void testNewBaseException() {
        assertEquals(new BaseException().getErrorType().getCode(), "-1");
    }

    @Test
    void testNewBaseExceptionWithErrorType() {
        BaseException baseException = new BaseException(SystemErrorType.ARGUMENT_NOT_VALID);
        assertEquals(baseException.getErrorType().getCode(), SystemErrorType.ARGUMENT_NOT_VALID.getCode());
    }

    @Test
    void testNewBaseExceptionWithErrorTypeAndMessage() {
        BaseException baseException = new BaseException(SystemErrorType.ARGUMENT_NOT_VALID, "无效参数");
        assertEquals(baseException.getErrorType().getCode(), SystemErrorType.ARGUMENT_NOT_VALID.getCode());
        assertEquals(baseException.getErrorType().getMesg(), "请求参数校验不通过");
        assertEquals(baseException.getMessage(), "无效参数");

    }

    @Test
    void testNewBaseExceptionWithErrorTypeAndMessageAndThrowable() {
        BaseException baseException = new BaseException(SystemErrorType.ARGUMENT_NOT_VALID, "无效参数", new RuntimeException());
        assertEquals(baseException.getErrorType().getCode(), SystemErrorType.ARGUMENT_NOT_VALID.getCode());
        assertEquals(baseException.getErrorType().getMesg(), "请求参数校验不通过");
        assertEquals(baseException.getMessage(), "无效参数");
    }
}