package com.learn.demo.dto.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UpdateUserRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validEmailPassesValidation() {
        UpdateUserRequest request = new UpdateUserRequest("user@example.com", "avatar.png");

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullEmailPassesValidation() {
        UpdateUserRequest request = new UpdateUserRequest(null, "avatar.png");

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidEmailFailsValidation() {
        UpdateUserRequest request = new UpdateUserRequest("bad@", "avatar.png");

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }
}
