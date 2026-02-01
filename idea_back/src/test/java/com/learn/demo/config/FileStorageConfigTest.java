package com.learn.demo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = FileStorageConfig.class)
@EnableConfigurationProperties(FileStorageConfig.class)
@TestPropertySource(properties = {
    "file.upload-dir=/tmp/uploads",
    "file.base-url=http://example.com",
    "file.max-file-size=12345",
    "file.allowed-types=jpg,png"
})
class FileStorageConfigTest {

    @Autowired
    private FileStorageConfig fileStorageConfig;

    @Test
    void propertiesAreBoundCorrectly() {
        assertEquals("/tmp/uploads", fileStorageConfig.getUploadDir());
        assertEquals("http://example.com", fileStorageConfig.getBaseUrl());
        assertEquals(12345L, fileStorageConfig.getMaxFileSize());
        assertIterableEquals(List.of("jpg", "png"), fileStorageConfig.getAllowedTypes());
    }

    @Test
    void defaultValuesAreApplied() {
        FileStorageConfig config = new FileStorageConfig();
        assertEquals("./uploads", config.getUploadDir());
        assertNull(config.getBaseUrl());
        assertEquals(5242880L, config.getMaxFileSize());
        assertIterableEquals(List.of("jpg", "jpeg", "png", "gif", "webp"), config.getAllowedTypes());
    }
}
