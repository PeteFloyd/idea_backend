package com.learn.demo.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.learn.demo.dto.ApiResponse;
import com.learn.demo.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@SpringBootTest(classes = GlobalExceptionHandlerTest.TestApp.class)
class GlobalExceptionHandlerTest {

    @SpringBootConfiguration
    @Import(GlobalExceptionHandler.class)
    static class TestApp {
    }

    @Autowired
    private GlobalExceptionHandler handler;

    @Test
    void handleBusinessExceptionUsesStatusBounds() {
        BusinessException lowCode = new BusinessException(400, "bad");
        ResponseEntity<ApiResponse<Void>> lowResponse = handler.handleBusinessException(lowCode);
        assertEquals(HttpStatus.BAD_REQUEST, lowResponse.getStatusCode());
        assertEquals(400, lowResponse.getBody().getCode());
        assertEquals("bad", lowResponse.getBody().getMessage());

        BusinessException highCode = new BusinessException(502, "upstream");
        ResponseEntity<ApiResponse<Void>> highResponse = handler.handleBusinessException(highCode);
        assertEquals(HttpStatus.BAD_GATEWAY, highResponse.getStatusCode());
        assertEquals(502, highResponse.getBody().getCode());
    }

    @Test
    void handleValidationExceptionReturnsFieldErrors() throws Exception {
        Method method = DummyController.class.getDeclaredMethod("submit", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "name", "blank"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);
        ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getCode());
        assertEquals("Validation failed", body.getMessage());
        List<ErrorResponse.FieldError> errors = body.getErrors();
        assertEquals(1, errors.size());
        assertEquals("name", errors.get(0).getField());
        assertEquals("blank", errors.get(0).getMessage());
    }

    @Test
    void handleExceptionReturnsInternalServerError() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleException(new RuntimeException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getCode());
        assertEquals("Internal server error", response.getBody().getMessage());
    }

    @Test
    void handleConstraintViolationException() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = Mockito.mock(ConstraintViolation.class);
        Path path = Mockito.mock(Path.class);
        Mockito.when(path.toString()).thenReturn("username");
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(violation.getMessage()).thenReturn("must not be blank");

        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));
        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getCode());
        assertEquals("Validation failed", body.getMessage());
        List<ErrorResponse.FieldError> errors = body.getErrors();
        assertEquals(1, errors.size());
        assertEquals("username", errors.get(0).getField());
        assertEquals("must not be blank", errors.get(0).getMessage());
    }

    private static class DummyController {
        @SuppressWarnings("unused")
        void submit(String name) {
        }
    }
}
