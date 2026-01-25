package com.learn.demo.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.learn.demo.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ResponseUtilTest {

    @Test
    void okWrapsSuccessResponse() {
        ResponseEntity<ApiResponse<String>> response = ResponseUtil.ok("value");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());
        assertEquals("success", response.getBody().getMessage());
        assertEquals("value", response.getBody().getData());
    }

    @Test
    void createdWrapsCreatedResponse() {
        ResponseEntity<ApiResponse<String>> response = ResponseUtil.created("value");
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(201, response.getBody().getCode());
        assertEquals("created", response.getBody().getMessage());
    }

    @Test
    void errorWrapsErrorResponse() {
        ResponseEntity<ApiResponse<Void>> response = ResponseUtil.error(HttpStatus.BAD_REQUEST, "bad");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getCode());
        assertEquals("bad", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        assertNotNull(response.getBody().getTimestamp());
    }
}
