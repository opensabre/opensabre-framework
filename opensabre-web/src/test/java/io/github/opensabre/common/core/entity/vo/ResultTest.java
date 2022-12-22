package io.github.opensabre.common.core.entity.vo;

import io.github.opensabre.common.core.exception.BaseException;
import io.github.opensabre.common.core.exception.SystemErrorType;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {
    @Test
    void testConstruct() {
        Result result = new Result();
        assertTrue(result.isSuccess());
        assertEquals(result.getCode(), "000000");
        assertEquals(result.getMesg(), "处理成功");
    }

    @Test
    void testConstructErrorType() {
        Result result = new Result(SystemErrorType.SYSTEM_BUSY);
        assertTrue(result.isFail());
        assertEquals(result.getCode(), SystemErrorType.SYSTEM_BUSY.getCode());
        assertEquals(result.getMesg(), SystemErrorType.SYSTEM_BUSY.getMesg());
    }

    @Test
    void testConstructErrorTypeWithData() {
        Date data = new Date();
        Result result = new Result(SystemErrorType.SYSTEM_BUSY, data);
        assertTrue(result.isFail());
        assertEquals(result.getCode(), SystemErrorType.SYSTEM_BUSY.getCode());
        assertEquals(result.getMesg(), SystemErrorType.SYSTEM_BUSY.getMesg());
        assertEquals(result.getData(), data);
    }

    @Test
    void testSuccess() {
        Result success = Result.success();
        assertTrue(success.isSuccess());
        assertEquals(success.getCode(), "000000");
        assertEquals(success.getMesg(), "处理成功");
    }

    @Test
    void testSuccessWithData() {
        Date data = new Date();
        Result result = Result.success(data);
        assertTrue(result.isSuccess());
        assertEquals(result.getData(), data);
    }

    @Test
    void testFail() {
        Result result = Result.fail();
        assertFalse(result.isSuccess());
        assertEquals(result.getCode(), "-1");
        assertEquals(result.getMesg(), "系统异常");
    }

    @Test
    void failWithData() {
        Date data = new Date();
        Result result = Result.fail(data);
        assertFalse(result.isSuccess());
        assertEquals(result.getData(), data);
    }

    @Test
    void testFailWithBaseException() {
        Result result = Result.fail(new BaseException());
        assertTrue(result.isFail());
        assertEquals(result.getCode(), SystemErrorType.SYSTEM_ERROR.getCode());
    }

    @Test
    void testFailWithBaseExceptionAndData() {
        Date data = new Date();
        Result result = Result.fail(new BaseException(), data);
        assertTrue(result.isFail());
        assertEquals(result.getCode(), SystemErrorType.SYSTEM_ERROR.getCode());
        assertEquals(result.getData(), data);
    }

    @Test
    void testFailWithErrorTypeAndData() {
        Date data = new Date();
        Result result = Result.fail(SystemErrorType.GATEWAY_ERROR, data);
        assertTrue(result.isFail());
        assertEquals(result.getCode(), SystemErrorType.GATEWAY_ERROR.getCode());
        assertEquals(result.getData(), data);
    }

    @Test
    void testFailWithErrorType() {
        Result result = Result.fail(SystemErrorType.GATEWAY_ERROR);
        assertTrue(result.isFail());
        assertEquals(result.getCode(), SystemErrorType.GATEWAY_ERROR.getCode());
    }

    @Test
    void isSuccess() {
        assertTrue(Result.success().isSuccess());
    }

    @Test
    void isFail() {
        assertTrue(Result.fail().isFail());
    }
}