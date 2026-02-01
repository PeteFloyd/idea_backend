package com.learn.demo.dto.idea;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class IdeaQueryRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void defaultValuesAppliedInNoArgsConstructor() {
        IdeaQueryRequest request = new IdeaQueryRequest();
        assertEquals(0, request.getPage());
        assertEquals(20, request.getSize());
        assertEquals("createdAt,desc", request.getSort());
    }

    @Test
    void defaultValuesAppliedInBuilder() {
        IdeaQueryRequest request = IdeaQueryRequest.builder().build();
        assertEquals(0, request.getPage());
        assertEquals(20, request.getSize());
        assertEquals("createdAt,desc", request.getSort());
    }

    @Test
    void sizeAboveMaxFailsValidation() {
        IdeaQueryRequest request = IdeaQueryRequest.builder()
            .size(101)
            .build();

        Set<ConstraintViolation<IdeaQueryRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("size", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void sizeAtMaxPassesValidation() {
        IdeaQueryRequest request = IdeaQueryRequest.builder()
            .size(100)
            .build();

        Set<ConstraintViolation<IdeaQueryRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullableFiltersDoNotFailValidation() {
        IdeaQueryRequest request = IdeaQueryRequest.builder()
            .keyword(null)
            .tag(null)
            .userId(null)
            .build();

        Set<ConstraintViolation<IdeaQueryRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
        assertNull(request.getKeyword());
        assertNull(request.getTag());
        assertNull(request.getUserId());
    }
}
