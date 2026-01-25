package com.learn.demo.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class BusinessExceptionTest {

    @Test
    void constructorWithCodeSetsFields() {
        BusinessException exception = new BusinessException(422, "invalid");
        assertEquals(422, exception.getCode());
        assertEquals("invalid", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructorWithMessageUsesDefaultCode() {
        BusinessException exception = new BusinessException("oops");
        assertEquals(400, exception.getCode());
        assertEquals("oops", exception.getMessage());
    }
}
