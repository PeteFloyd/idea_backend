package com.learn.demo.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorResponseTest {

    @Test
    void builderSetsFieldsAndTimestamp() {
        ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError("field", "message");
        ErrorResponse response = ErrorResponse.builder()
            .code(400)
            .message("bad")
            .errors(List.of(fieldError))
            .build();

        assertEquals(400, response.getCode());
        assertEquals("bad", response.getMessage());
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void fieldErrorDataMethodsWork() {
        ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError();
        fieldError.setField("f");
        fieldError.setMessage("m");

        ErrorResponse.FieldError other = new ErrorResponse.FieldError("f", "m");
        assertEquals(fieldError.getField(), other.getField());
        assertEquals(fieldError.getMessage(), other.getMessage());
        assertEquals(fieldError, other);
        assertTrue(fieldError.toString().contains("f"));
    }
}
