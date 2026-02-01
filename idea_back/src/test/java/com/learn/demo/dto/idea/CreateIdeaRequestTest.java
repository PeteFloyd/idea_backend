package com.learn.demo.dto.idea;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CreateIdeaRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validRequestPassesValidation() {
        CreateIdeaRequest request = CreateIdeaRequest.builder()
            .title("a")
            .description("b")
            .images(List.of())
            .tags(List.of("tag"))
            .build();

        Set<ConstraintViolation<CreateIdeaRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankTitleFailsValidation() {
        CreateIdeaRequest request = CreateIdeaRequest.builder()
            .title(" ")
            .description("desc")
            .build();

        Set<ConstraintViolation<CreateIdeaRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("title", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void titleTooLongFailsValidation() {
        CreateIdeaRequest request = CreateIdeaRequest.builder()
            .title(repeat("a", 101))
            .description("desc")
            .build();

        Set<ConstraintViolation<CreateIdeaRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("title", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void descriptionTooLongFailsValidation() {
        CreateIdeaRequest request = CreateIdeaRequest.builder()
            .title("title")
            .description(repeat("a", 5001))
            .build();

        Set<ConstraintViolation<CreateIdeaRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("description", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void tooManyImagesFailsValidation() {
        List<String> images = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            images.add("img" + i);
        }

        CreateIdeaRequest request = CreateIdeaRequest.builder()
            .title("title")
            .description("desc")
            .images(images)
            .build();

        Set<ConstraintViolation<CreateIdeaRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("images", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void nullTitleFailsValidation() {
        CreateIdeaRequest request = CreateIdeaRequest.builder()
            .title(null)
            .description("desc")
            .build();

        Set<ConstraintViolation<CreateIdeaRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("title", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void nullDescriptionFailsValidation() {
        CreateIdeaRequest request = CreateIdeaRequest.builder()
            .title("title")
            .description(null)
            .build();

        Set<ConstraintViolation<CreateIdeaRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("description", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void boundaryLengthsPassValidation() {
        CreateIdeaRequest request = CreateIdeaRequest.builder()
            .title(repeat("a", 100))
            .description(repeat("b", 5000))
            .images(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9"))
            .build();

        Set<ConstraintViolation<CreateIdeaRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    private static String repeat(String value, int count) {
        return value.repeat(count);
    }
}
