package com.learn.demo.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ImagesConverterTest {

    private final ImagesConverter converter = new ImagesConverter();

    @Test
    void convertToDatabaseColumnHandlesNullAndEmpty() {
        assertEquals("[]", converter.convertToDatabaseColumn(null));
        assertEquals("[]", converter.convertToDatabaseColumn(List.of()));
    }

    @Test
    void convertToDatabaseColumnSerializesList() {
        assertEquals("[\"a\",\"b\"]", converter.convertToDatabaseColumn(List.of("a", "b")));
    }

    @Test
    void convertToEntityAttributeHandlesNullAndBlank() {
        assertTrue(converter.convertToEntityAttribute(null).isEmpty());
        assertTrue(converter.convertToEntityAttribute(" ").isEmpty());
    }

    @Test
    void convertToEntityAttributeParsesJson() {
        assertEquals(List.of("a", "b"), converter.convertToEntityAttribute("[\"a\",\"b\"]"));
    }

    @Test
    void convertToEntityAttributeReturnsEmptyOnInvalidJson() {
        assertTrue(converter.convertToEntityAttribute("not-json").isEmpty());
    }
}
