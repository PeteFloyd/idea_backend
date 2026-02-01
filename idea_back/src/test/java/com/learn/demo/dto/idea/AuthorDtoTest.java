package com.learn.demo.dto.idea;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.learn.demo.entity.User;
import org.junit.jupiter.api.Test;

class AuthorDtoTest {

    @Test
    void fromUserMapsFields() {
        User user = new User();
        user.setId(5L);
        user.setUsername("alice");
        user.setAvatar("avatar.png");

        AuthorDto dto = AuthorDto.fromUser(user);
        assertEquals(5L, dto.getId());
        assertEquals("alice", dto.getUsername());
        assertEquals("avatar.png", dto.getAvatar());
    }

    @Test
    void fromUserReturnsNullWhenUserNull() {
        assertNull(AuthorDto.fromUser(null));
    }
}
