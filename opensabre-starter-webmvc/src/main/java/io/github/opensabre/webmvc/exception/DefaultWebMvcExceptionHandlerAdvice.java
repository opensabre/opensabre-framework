package io.github.opensabre.webmvc.exception;

import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.common.core.exception.SystemErrorType;
import jakarta.servlet.ServletException;
import io.github.opensabre.common.core.exception.BaseException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.Optional;

/**
 * 默认全局异常处理类
 */
@Slf4j
@Order
@RestControllerAdvice
public class DefaultWebMvcExceptionHandlerAdvice {

    @ExceptionHandler(value = {MissingServletRequestParameterException.class})
    public Result<?> missingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.warn("missing servlet request parameter exception:{}", ex.getMessage());
        return Result.fail(SystemErrorType.ARGUMENT_NOT_VALID);
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public Result<?> argumentInvalidException(MethodArgumentNotValidException ex) {
        log.warn("service exception:{}", ex.getMessage());
        return Result.fail(SystemErrorType.ARGUMENT_NOT_VALID, ex.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ExceptionHandler(value = {HandlerMethodValidationException.class})
    public Result<?> handlerMethodValidationException(HandlerMethodValidationException ex) {
        log.warn("handler method validation exception:{}", ex.getMessage());
        return Result.fail(SystemErrorType.ARGUMENT_NOT_VALID, ex.getMessage());
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    public Result<?> constraintViolationException(ConstraintViolationException ex) {
        log.warn("constraint violation exception:{}", ex.getMessage());
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse(ex.getMessage());
        return Result.fail(SystemErrorType.ARGUMENT_NOT_VALID, message);
    }

    /**
     * 处理校验器执行期间的基础设施异常，例如字典中心不可用。
     */
    @ExceptionHandler(value = {ValidationException.class})
    public ResponseEntity<Result<?>> validationException(ValidationException ex) {
        Optional<BaseException> baseException = findCause(ex, BaseException.class);
        if (baseException.isPresent()) {
            log.error("validation dependency unavailable: {}", baseException.get().getMessage(), ex);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Result.fail(baseException.get().getErrorType()));
        }
        log.error("validation engine exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.fail());
    }

    @ExceptionHandler(value = {HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public Result<?> httpMessageConvertException(Exception ex) {
        log.warn("http message convert exception:{}", ex.getMessage());
        return Result.fail(SystemErrorType.ARGUMENT_NOT_VALID, "数据解析错误：" + ex.getMessage());
    }

    @ExceptionHandler(value = {MultipartException.class})
    public Result<?> uploadFileLimitException(MultipartException ex) {
        log.warn("upload file size limit:{}", ex.getMessage());
        return Result.fail(SystemErrorType.UPLOAD_FILE_SIZE_LIMIT);
    }

    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> notSupportedMethodException(HttpRequestMethodNotSupportedException ex) {
        log.warn("http request method not supported exception {}", ex.getMessage());
        return Result.fail(SystemErrorType.METHOD_NOT_SUPPORTED);
    }

    @ExceptionHandler(value = {NoHandlerFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> noHandlerFoundException(ServletException ex) {
        log.warn("No static resource exception:{}", ex.getMessage());
        return Result.fail(SystemErrorType.RESOURCE_NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(value = {HttpMediaTypeNotSupportedException.class})
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<?> notSupportedMethodException(HttpMediaTypeNotSupportedException ex) {
        log.warn("http request media not supported exception {}", ex.getMessage());
        return Result.fail(SystemErrorType.METHOD_NOT_SUPPORTED);
    }

    @ExceptionHandler(value = {BaseException.class})
    public Result<?> baseException(BaseException ex) {
        log.warn("base exception:{}", ex.getMessage());
        return Result.fail(ex.getErrorType());
    }

    @ExceptionHandler(value = {Exception.class, Throwable.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> exception(Throwable ex) {
        log.error("exception: ", ex);
        return Result.fail();
    }

    private <T extends Throwable> Optional<T> findCause(Throwable throwable, Class<T> causeType) {
        Throwable current = throwable;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return Optional.of(causeType.cast(current));
            }
            current = current.getCause();
        }
        return Optional.empty();
    }
}
