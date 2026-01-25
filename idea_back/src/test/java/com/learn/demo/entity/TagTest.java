package com.learn.demo.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class TagTest {

    @Test
    void testEqualsAndHashCode() {
        Tag tag1 = new Tag();
        tag1.setId(1L);

        Tag tag2 = new Tag();
        tag2.setId(1L);

        Tag tag3 = new Tag();
        tag3.setId(2L);

        // Same object
        assertEquals(tag1, tag1);

        // Same id
        assertEquals(tag1, tag2);

        // Different id
        assertNotEquals(tag1, tag3);

        // Null
        assertNotEquals(tag1, null);

        // Different type
        assertNotEquals(tag1, "string");

        // Null id - both null
        Tag tag4 = new Tag();
        Tag tag5 = new Tag();
        assertNotEquals(tag4, tag5);

        // Null id vs non-null id
        assertNotEquals(tag4, tag1);

        // hashCode consistency
        assertEquals(tag1.hashCode(), tag2.hashCode());
    }
}
