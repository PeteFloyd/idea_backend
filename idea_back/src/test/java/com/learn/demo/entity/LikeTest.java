package com.learn.demo.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class LikeTest {

    @Test
    void testEqualsAndHashCode() {
        Like like1 = new Like();
        like1.setId(1L);

        Like like2 = new Like();
        like2.setId(1L);

        Like like3 = new Like();
        like3.setId(2L);

        // Same object
        assertEquals(like1, like1);

        // Same id
        assertEquals(like1, like2);

        // Different id
        assertNotEquals(like1, like3);

        // Null
        assertNotEquals(like1, null);

        // Different type
        assertNotEquals(like1, "string");

        // Null id - both null
        Like like4 = new Like();
        Like like5 = new Like();
        assertNotEquals(like4, like5);

        // Null id vs non-null id
        assertNotEquals(like4, like1);

        // hashCode consistency
        assertEquals(like1.hashCode(), like2.hashCode());
    }
}
