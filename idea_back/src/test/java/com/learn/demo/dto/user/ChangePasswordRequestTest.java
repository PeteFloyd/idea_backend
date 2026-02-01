package com.learn.demo.dto.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ChangePasswordRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validChangePasswordRequestPassesValidation() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldpass", "newpass1");

        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankOldPasswordFailsValidation() {
        ChangePasswordRequest request = new ChangePasswordRequest(" ", "newpass1");

        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("oldPassword", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void blankNewPasswordFailsValidation() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldpass", " ");

        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
        assertTrue(violations.stream()
            .allMatch(violation -> "newPassword".equals(violation.getPropertyPath().toString())));
    }

    @Test
    void newPasswordTooShortFailsValidation() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldpass", "12345");

        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("newPassword", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void newPasswordTooLongFailsValidation() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldpass", "a".repeat(33));

        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("newPassword", violations.iterator().next().getPropertyPath().toString());
    }
}
