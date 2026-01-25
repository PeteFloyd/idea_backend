package com.learn.demo.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class ReportTest {

    @Test
    void testEqualsAndHashCode() {
        Report report1 = new Report();
        report1.setId(1L);

        Report report2 = new Report();
        report2.setId(1L);

        Report report3 = new Report();
        report3.setId(2L);

        // Same object
        assertEquals(report1, report1);

        // Same id
        assertEquals(report1, report2);

        // Different id
        assertNotEquals(report1, report3);

        // Null
        assertNotEquals(report1, null);

        // Different type
        assertNotEquals(report1, "string");

        // Null id - both null
        Report report4 = new Report();
        Report report5 = new Report();
        assertNotEquals(report4, report5);

        // Null id vs non-null id
        assertNotEquals(report4, report1);

        // hashCode consistency
        assertEquals(report1.hashCode(), report2.hashCode());
    }
}
