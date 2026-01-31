package com.learn.demo.dto.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LoginRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validLoginRequestPassesValidation() {
        LoginRequest request = LoginRequest.builder()
            .username("user123")
            .password("secret12")
            .build();

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankUsernameFailsValidation() {
        LoginRequest request = LoginRequest.builder()
            .username("")
            .password("secret12")
            .build();

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("username", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void blankPasswordFailsValidation() {
        LoginRequest request = LoginRequest.builder()
            .username("user123")
            .password(" ")
            .build();

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("password", violations.iterator().next().getPropertyPath().toString());
    }
}
