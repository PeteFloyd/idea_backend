package com.learn.demo.dto.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.learn.demo.entity.User;
import com.learn.demo.enums.UserRole;
import com.learn.demo.enums.UserStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class UserResponseTest {

    @Test
    void fromUserMapsFieldsCorrectly() {
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 2, 3, 4, 5);
        User user = new User();
        user.setId(10L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setAvatar("avatar.png");
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(createdAt);

        UserResponse response = UserResponse.fromUser(user);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("alice", response.getUsername());
        assertEquals("alice@example.com", response.getEmail());
        assertEquals("avatar.png", response.getAvatar());
        assertEquals("ADMIN", response.getRole());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void serializesToExpectedJson() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2024, 12, 31, 23, 59, 0);
        UserResponse response = UserResponse.builder()
            .id(7L)
            .username("bob")
            .email("bob@example.com")
            .avatar("bob.png")
            .role("USER")
            .status("ACTIVE")
            .createdAt(createdAt)
            .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonNode node = mapper.readTree(mapper.writeValueAsString(response));

        assertEquals(7L, node.get("id").asLong());
        assertEquals("bob", node.get("username").asText());
        assertEquals("bob@example.com", node.get("email").asText());
        assertEquals("bob.png", node.get("avatar").asText());
        assertEquals("USER", node.get("role").asText());
        assertEquals("ACTIVE", node.get("status").asText());
        assertEquals("2024-12-31T23:59:00", node.get("createdAt").asText());
    }
}
