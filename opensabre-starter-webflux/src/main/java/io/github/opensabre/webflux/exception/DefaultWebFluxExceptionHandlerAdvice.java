package io.github.opensabre.webflux.exception;

import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.common.core.exception.BaseException;
import io.github.opensabre.common.core.exception.SystemErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

/**
 * Default global exception handler for Spring WebFlux applications.
 */
@Slf4j
@Order
@RestControllerAdvice
public class DefaultWebFluxExceptionHandlerAdvice {

    @ExceptionHandler(value = {ServerWebInputException.class})
    public Result<?> serverWebInputException(ServerWebInputException ex) {
        log.warn("server web input exception:{}", ex.getMessage());
        return Result.fail(SystemErrorType.ARGUMENT_NOT_VALID, "数据解析错误：" + ex.getReason());
    }

    @ExceptionHandler(value = {WebExchangeBindException.class})
    public Result<?> webExchangeBindException(WebExchangeBindException ex) {
        log.warn("web exchange bind exception:{}", ex.getMessage());
        if (ex.getFieldError() == null) {
            return Result.fail(SystemErrorType.ARGUMENT_NOT_VALID);
        }
        return Result.fail(SystemErrorType.ARGUMENT_NOT_VALID, ex.getFieldError().getDefaultMessage());
    }

    @ExceptionHandler(value = {HandlerMethodValidationException.class})
    public Result<?> handlerMethodValidationException(HandlerMethodValidationException ex) {
        log.warn("handler method validation exception:{}", ex.getMessage());
        return Result.fail(SystemErrorType.ARGUMENT_NOT_VALID, ex.getMessage());
    }

    @ExceptionHandler(value = {MethodNotAllowedException.class})
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> methodNotAllowedException(MethodNotAllowedException ex) {
        log.warn("request method not supported exception:{}", ex.getMessage());
        return Result.fail(SystemErrorType.METHOD_NOT_SUPPORTED);
    }

    @ExceptionHandler(value = {NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> noResourceFoundException(NoResourceFoundException ex) {
        log.warn("no resource found exception:{}", ex.getMessage());
        return Result.fail(SystemErrorType.RESOURCE_NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(value = {UnsupportedMediaTypeStatusException.class})
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<?> unsupportedMediaTypeStatusException(UnsupportedMediaTypeStatusException ex) {
        log.warn("http request media not supported exception:{}", ex.getMessage());
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
        log.error("system exception", ex);
        return Result.fail();
    }
}
