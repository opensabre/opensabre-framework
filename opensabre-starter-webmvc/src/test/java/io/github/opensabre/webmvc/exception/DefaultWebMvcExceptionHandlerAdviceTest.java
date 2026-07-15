package io.github.opensabre.webmvc.exception;

import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.common.core.exception.BaseException;
import io.github.opensabre.common.core.exception.SystemErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MultipartException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DefaultWebMvcExceptionHandlerAdviceTest {

    private DefaultWebMvcExceptionHandlerAdvice advice;

    @BeforeEach
    void before() {
        this.advice = new DefaultWebMvcExceptionHandlerAdvice();
    }

    @Test
    void missingServletRequestParameterExceptionShouldReturnArgumentInvalid() {
        Result<?> result = advice
                .missingServletRequestParameterException(new MissingServletRequestParameterException("test", "string"));
        assertEquals(SystemErrorType.ARGUMENT_NOT_VALID.getCode(), result.getCode());
    }

    @Test
    void uploadFileLimitExceptionShouldReturnUploadLimit() {
        Result<?> result = advice.uploadFileLimitException(new MultipartException("test"));
        assertEquals(SystemErrorType.UPLOAD_FILE_SIZE_LIMIT.getCode(), result.getCode());
    }

    @Test
    void methodArgumentNotValidExceptionShouldReturnFieldMessage() throws NoSuchMethodException {
        MethodParameter methodParameter = new MethodParameter(Object.class.getMethod("toString"), -1);
        BindException bindingResult = new BindException(new Object(), "test");
        bindingResult.addError(new FieldError("test", "test", "testmessage"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        Result<?> result = advice.argumentInvalidException(exception);

        assertEquals(SystemErrorType.ARGUMENT_NOT_VALID.getCode(), result.getCode());
        assertEquals(SystemErrorType.ARGUMENT_NOT_VALID.getMesg() + "：testmessage", result.getMesg());
        assertNull(result.getData());
    }

    @Test
    void baseExceptionShouldReturnOriginalErrorType() {
        Result<?> result = advice.baseException(new BaseException(SystemErrorType.SYSTEM_BUSY));
        assertEquals(SystemErrorType.SYSTEM_BUSY.getCode(), result.getCode());
    }

    @Test
    void exceptionShouldReturnSystemError() {
        Result<?> result = advice.exception(new Exception("error"));
        assertEquals(SystemErrorType.SYSTEM_ERROR.getCode(), result.getCode());
    }

    @Test
    void throwableShouldReturnSystemError() {
        Result<?> result = advice.exception(new Throwable("error"));
        assertEquals(SystemErrorType.SYSTEM_ERROR.getCode(), result.getCode());
    }
}
