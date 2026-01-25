package com.learn.demo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@SpringBootTest(
    classes = CorsConfig.class,
    properties = {
        "spring.web.cors.allowed-origins=https://a.com,https://b.com",
        "spring.web.cors.allowed-methods=GET,POST"
    }
)
class CorsConfigTest {

    @Autowired
    private CorsConfig corsConfig;

    @Test
    void addCorsMappingsUsesConfiguredOriginsAndMethods() throws Exception {
        CorsRegistry registry = new CorsRegistry();
        corsConfig.addCorsMappings(registry);

        Map<String, CorsConfiguration> configs = getCorsConfigurations(registry);
        assertEquals(1, configs.size());

        CorsConfiguration config = configs.get("/**");
        assertNotNull(config);
        assertEquals(List.of("https://a.com", "https://b.com"), config.getAllowedOrigins());
        assertEquals(List.of("GET", "POST"), config.getAllowedMethods());
        assertEquals(List.of("*"), config.getAllowedHeaders());
        assertFalse(Boolean.TRUE.equals(config.getAllowCredentials()));
    }

    @SuppressWarnings("unchecked")
    private Map<String, CorsConfiguration> getCorsConfigurations(CorsRegistry registry) throws Exception {
        try {
            Method method = CorsRegistry.class.getDeclaredMethod("getCorsConfigurations");
            method.setAccessible(true);
            return (Map<String, CorsConfiguration>) method.invoke(registry);
        } catch (NoSuchMethodException ignored) {
            Field field = CorsRegistry.class.getDeclaredField("corsConfigurations");
            field.setAccessible(true);
            return (Map<String, CorsConfiguration>) field.get(registry);
        }
    }
}
