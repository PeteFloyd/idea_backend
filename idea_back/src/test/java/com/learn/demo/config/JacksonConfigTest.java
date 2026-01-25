package com.learn.demo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = JacksonConfig.class)
class JacksonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void objectMapperUsesUtcAndIsoDateFormat() {
        assertEquals("UTC", objectMapper.getSerializationConfig().getTimeZone().getID());
        String formatted = objectMapper.getDateFormat().format(new Date(0));
        assertEquals("1970-01-01T00:00:00Z", formatted);
    }

    @Test
    void objectMapperDisablesTimestampsAndRegistersJavaTimeModule() {
        assertFalse(objectMapper.getSerializationConfig().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        boolean hasJavaTime = objectMapper.getRegisteredModuleIds().stream()
            .anyMatch(id -> id.toString().contains("jsr310") || id.toString().contains("JavaTimeModule"));
        assertTrue(hasJavaTime);
    }
}
