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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 *
 */
@Slf4j
@RestControllerAdvice
@Order(100)
public class SentinelExceptionHandlerAdvice {

    @ExceptionHandler(BlockException.class)
    public Result blockException(BlockException e) {
        log.error("block exception:{}", e.getMessage());
        return Result.fail(SystemErrorType.SYSTEM_BUSY);
    }

    @ExceptionHandler(FlowException.class)
    public Result flowException(FlowException e) {
        log.error("flow exception:{}", e.getMessage());
        return Result.fail(SystemErrorType.SYSTEM_BUSY);
    }

    @ExceptionHandler(DegradeException.class)
    public Result degradeException(DegradeException e) {
        log.error("degrade exception:{}", e.getMessage());
        return Result.fail(SystemErrorType.SYSTEM_BUSY);
    }

    @ExceptionHandler(ParamFlowException.class)
    public Result paramFlowException(ParamFlowException e) {
        log.error("param flow exception:{}", e.getMessage());
        return Result.fail(SystemErrorType.SYSTEM_BUSY);
    }

    @ExceptionHandler(SystemBlockException.class)
    public Result systemBlockException(SystemBlockException e) {
        log.error("system block exception:{}", e.getMessage());
        return Result.fail(SystemErrorType.SYSTEM_BUSY);
    }

    @ExceptionHandler(AuthorityException.class)
    public Result authorityException(AuthorityException e) {
        log.error("authority exception:{}", e.getMessage());
        return Result.fail(SystemErrorType.SYSTEM_BUSY);
    }
}
