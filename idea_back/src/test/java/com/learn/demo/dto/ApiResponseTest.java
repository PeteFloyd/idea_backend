package com.learn.demo.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void successWithDataBuildsExpectedResponse() {
        ApiResponse<String> response = ApiResponse.success("data");
        assertEquals(200, response.getCode());
        assertEquals("success", response.getMessage());
        assertEquals("data", response.getData());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void successWithoutDataUsesNullData() {
        ApiResponse<Void> response = ApiResponse.success();
        assertEquals(200, response.getCode());
        assertNull(response.getData());
        assertNotNull(response.getMessage());
    }

    @Test
    void errorBuildsExpectedResponse() {
        ApiResponse<Void> response = ApiResponse.error(404, "not found");
        assertEquals(404, response.getCode());
        assertEquals("not found", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void dataMethodsAndEqualityWork() {
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(1);
        response.setMessage("m");
        response.setData("d");
        response.setTimestamp(123L);

        ApiResponse<String> other = new ApiResponse<>(1, "m", "d", 123L);
        assertEquals(response, other);
        assertEquals(response.hashCode(), other.hashCode());
        assertTrue(response.toString().contains("m"));
    }
}
