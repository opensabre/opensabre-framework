package io.github.opensabre.persistence.exception;

import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.common.core.exception.SystemErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistenceExceptionHandlerAdviceTest {

    private PersistenceExceptionHandlerAdvice persistenceExceptionHandlerAdvice;

    @BeforeEach
    public void before() {
        this.persistenceExceptionHandlerAdvice = new PersistenceExceptionHandlerAdvice();
    }

    @Test
    public void testDuplicateKeyException() {
        Result result = persistenceExceptionHandlerAdvice.duplicateKeyException(new DuplicateKeyException("主键重复"));
        assertEquals(SystemErrorType.DUPLICATE_PRIMARY_KEY.getCode(), result.getCode());
    }
}