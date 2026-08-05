package io.github.opensabre.webmvc.exception;

import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.common.core.exception.BaseException;
import io.github.opensabre.common.core.exception.SystemErrorType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MultipartException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void constraintViolationShouldReturnArgumentInvalidMessage() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("性别必须是有效字典项");
        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        Result<?> result = advice.constraintViolationException(exception);

        assertEquals(SystemErrorType.ARGUMENT_NOT_VALID.getCode(), result.getCode());
        assertEquals(SystemErrorType.ARGUMENT_NOT_VALID.getMesg() + "：性别必须是有效字典项", result.getMesg());
    }

    @Test
    void validationDependencyFailureShouldReturnServiceUnavailable() {
        BaseException unavailable = new BaseException(SystemErrorType.SYSTEM_BUSY, "dictionary unavailable");
        ValidationException exception = new ValidationException("validator failed", unavailable);

        var response = advice.validationException(exception);

        assertEquals(503, response.getStatusCode().value());
        assertEquals(SystemErrorType.SYSTEM_BUSY.getCode(), response.getBody().getCode());
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
