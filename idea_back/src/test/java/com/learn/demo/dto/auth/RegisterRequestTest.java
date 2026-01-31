package com.learn.demo.dto.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RegisterRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validRegisterRequestPassesValidation() {
        RegisterRequest request = RegisterRequest.builder()
            .username("user123")
            .password("secret12")
            .email("user@example.com")
            .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void usernameTooShortFailsValidation() {
        RegisterRequest request = buildRequest("ab", "secret12", "user@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("username", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void usernameTooLongFailsValidation() {
        RegisterRequest request = buildRequest("a".repeat(21), "secret12", "user@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("username", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void usernameWithSpecialCharactersFailsValidation() {
        RegisterRequest request = buildRequest("user!", "secret12", "user@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("username", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void passwordTooShortFailsValidation() {
        RegisterRequest request = buildRequest("user123", "12345", "user@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("password", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void passwordTooLongFailsValidation() {
        RegisterRequest request = buildRequest("user123", "a".repeat(33), "user@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("password", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void invalidEmailFailsValidation() {
        RegisterRequest request = buildRequest("user123", "secret12", "bad@");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }

    private RegisterRequest buildRequest(String username, String password, String email) {
        return RegisterRequest.builder()
            .username(username)
            .password(password)
            .email(email)
            .build();
    }
}
