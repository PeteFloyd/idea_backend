package com.learn.demo.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class IdeaTest {

    @Test
    void testEqualsAndHashCode() {
        Idea idea1 = new Idea();
        idea1.setId(1L);

        Idea idea2 = new Idea();
        idea2.setId(1L);

        Idea idea3 = new Idea();
        idea3.setId(2L);

        // Same object
        assertEquals(idea1, idea1);

        // Same id
        assertEquals(idea1, idea2);

        // Different id
        assertNotEquals(idea1, idea3);

        // Null
        assertNotEquals(idea1, null);

        // Different type
        assertNotEquals(idea1, "string");

        // Null id - both null
        Idea idea4 = new Idea();
        Idea idea5 = new Idea();
        assertNotEquals(idea4, idea5);

        // Null id vs non-null id
        assertNotEquals(idea4, idea1);

        // hashCode consistency
        assertEquals(idea1.hashCode(), idea2.hashCode());
    }
}
