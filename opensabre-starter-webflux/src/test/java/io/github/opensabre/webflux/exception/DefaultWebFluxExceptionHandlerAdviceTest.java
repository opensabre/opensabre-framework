package io.github.opensabre.webflux.exception;

import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.common.core.exception.BaseException;
import io.github.opensabre.common.core.exception.SystemErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultWebFluxExceptionHandlerAdviceTest {

    private DefaultWebFluxExceptionHandlerAdvice advice;

    @BeforeEach
    void setUp() {
        this.advice = new DefaultWebFluxExceptionHandlerAdvice();
    }

    @Test
    void serverWebInputExceptionShouldReturnArgumentNotValid() {
        Result<?> result = advice.serverWebInputException(new ServerWebInputException("bad request"));

        assertEquals(SystemErrorType.ARGUMENT_NOT_VALID.getCode(), result.getCode());
    }

    @Test
    void unsupportedMediaTypeShouldReturnMethodNotSupported() {
        Result<?> result = advice.unsupportedMediaTypeStatusException(new UnsupportedMediaTypeStatusException("json"));

        assertEquals(SystemErrorType.METHOD_NOT_SUPPORTED.getCode(), result.getCode());
    }

    @Test
    void baseExceptionShouldReturnOriginalErrorType() {
        Result<?> result = advice.baseException(new BaseException(SystemErrorType.SYSTEM_BUSY));

        assertEquals(SystemErrorType.SYSTEM_BUSY.getCode(), result.getCode());
    }

    @Test
    void unknownExceptionShouldReturnSystemError() {
        Result<?> result = advice.exception(new IllegalStateException("error"));

        assertEquals(SystemErrorType.SYSTEM_ERROR.getCode(), result.getCode());
    }
}
