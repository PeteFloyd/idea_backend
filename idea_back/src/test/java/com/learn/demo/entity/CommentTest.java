package com.learn.demo.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class CommentTest {

    @Test
    void testEqualsAndHashCode() {
        Comment comment1 = new Comment();
        comment1.setId(1L);

        Comment comment2 = new Comment();
        comment2.setId(1L);

        Comment comment3 = new Comment();
        comment3.setId(2L);

        // Same object
        assertEquals(comment1, comment1);

        // Same id
        assertEquals(comment1, comment2);

        // Different id
        assertNotEquals(comment1, comment3);

        // Null
        assertNotEquals(comment1, null);

        // Different type
        assertNotEquals(comment1, "string");

        // Null id - both null
        Comment comment4 = new Comment();
        Comment comment5 = new Comment();
        assertNotEquals(comment4, comment5);

        // Null id vs non-null id
        assertNotEquals(comment4, comment1);

        // hashCode consistency
        assertEquals(comment1.hashCode(), comment2.hashCode());
    }
}
