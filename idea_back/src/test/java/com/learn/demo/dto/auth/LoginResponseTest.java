package com.learn.demo.dto.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class LoginResponseTest {

    @Test
    void builderSetsDefaultTokenTypeAndFields() {
        LoginResponse.UserInfo user = LoginResponse.UserInfo.builder()
            .id(1L)
            .username("user123")
            .role("USER")
            .build();

        LoginResponse response = LoginResponse.builder()
            .token("token")
            .expiresIn(3600L)
            .user(user)
            .build();

        assertEquals("token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        assertNotNull(response.getUser());
    }

    @Test
    void serializesToExpectedJson() throws Exception {
        LoginResponse.UserInfo user = LoginResponse.UserInfo.builder()
            .id(7L)
            .username("alice")
            .role("ADMIN")
            .build();

        LoginResponse response = LoginResponse.builder()
            .token("jwt")
            .expiresIn(7200L)
            .user(user)
            .build();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(mapper.writeValueAsString(response));

        assertEquals("jwt", node.get("token").asText());
        assertEquals("Bearer", node.get("tokenType").asText());
        assertEquals(7200L, node.get("expiresIn").asLong());
        assertEquals(7L, node.get("user").get("id").asLong());
        assertEquals("alice", node.get("user").get("username").asText());
        assertEquals("ADMIN", node.get("user").get("role").asText());
    }
}
