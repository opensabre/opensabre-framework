package io.github.opensabre.rpc.sentinel.exception;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.common.core.exception.SystemErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 *
 */
@Slf4j
@RestControllerAdvice
@Order(100)
public class SentinelExceptionHandlerAdvice {

    @ExceptionHandler(BlockException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result blockException(BlockException e) {
        log.error("block exception:{}", e.getRule());
        return Result.fail(SystemErrorType.SYSTEM_BUSY);
    }

    @ExceptionHandler(FlowException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result flowException(FlowException e) {
        log.error("flow exception:{}", e.getRule());
        return Result.fail(SystemErrorType.SYSTEM_BUSY);
    }

    @ExceptionHandler(DegradeException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result degradeException(DegradeException e) {
        log.error("degrade exception:{}", e.getRule());
        return Result.fail(SystemErrorType.SYSTEM_BUSY);
    }

    @ExceptionHandler(ParamFlowException.class)
    public Result paramFlowException(ParamFlowException e) {
        log.error("param flow exception:{}", e.getRule());
        return Result.fail(SystemErrorType.SYSTEM_BUSY);
    }

    @ExceptionHandler(SystemBlockException.class)
    public Result systemBlockException(SystemBlockException e) {
        log.error("system block exception:{}", e.getRule());
        return Result.fail(SystemErrorType.SYSTEM_BUSY);
    }

    @ExceptionHandler(AuthorityException.class)
    public Result authorityException(AuthorityException e) {
        log.error("authority exception:{}", e.getRule());
        return Result.fail(SystemErrorType.SYSTEM_BUSY);
    }
}
