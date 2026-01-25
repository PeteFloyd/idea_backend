package com.learn.demo.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void testEqualsAndHashCode() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(1L);

        User user3 = new User();
        user3.setId(2L);

        // Same object
        assertEquals(user1, user1);

        // Same id
        assertEquals(user1, user2);

        // Different id
        assertNotEquals(user1, user3);

        // Null
        assertNotEquals(user1, null);

        // Different type
        assertNotEquals(user1, "string");

        // Null id - both null
        User user4 = new User();
        User user5 = new User();
        assertNotEquals(user4, user5);

        // Null id vs non-null id
        assertNotEquals(user4, user1);

        // hashCode consistency
        assertEquals(user1.hashCode(), user2.hashCode());
    }
}
