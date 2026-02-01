package com.learn.demo.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.learn.demo.config.FileStorageConfig;
import com.learn.demo.dto.file.FileUploadResponse;
import com.learn.demo.exception.BusinessException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileUploadServiceTest {

    @Mock
    private FileStorageConfig fileStorageConfig;

    private FileUploadService fileUploadService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileUploadService = new FileUploadService(fileStorageConfig);
        when(fileStorageConfig.getUploadDir()).thenReturn(tempDir.toString());
        when(fileStorageConfig.getBaseUrl()).thenReturn("http://localhost:8080");
        when(fileStorageConfig.getMaxFileSize()).thenReturn(5_242_880L);
        when(fileStorageConfig.getAllowedTypes()).thenReturn(List.of("jpg", "jpeg", "png", "gif", "webp"));
    }

    @Test
    void upload_withValidImage_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "avatar.png",
            "image/png",
            "image-content".getBytes()
        );

        FileUploadResponse response = fileUploadService.upload(file);

        assertNotNull(response);
        assertTrue(response.getUrl().startsWith("http://localhost:8080/uploads/"));
        assertTrue(response.getFilename().endsWith(".png"));
        assertEquals(file.getSize(), response.getSize());
        assertTrue(Files.exists(tempDir.resolve(response.getFilename())));
    }

    @Test
    void upload_withEmptyFile_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.png",
            "image/png",
            new byte[0]
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> fileUploadService.upload(file));
        assertEquals(400, exception.getCode());
        assertEquals("File is empty", exception.getMessage());
    }

    @Test
    void upload_withOversizedFile_throwsException() {
        when(fileStorageConfig.getMaxFileSize()).thenReturn(3L);
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "large.png",
            "image/png",
            new byte[] {1, 2, 3, 4}
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> fileUploadService.upload(file));
        assertEquals(400, exception.getCode());
        assertEquals("File size exceeds limit", exception.getMessage());
    }

    @Test
    void upload_withInvalidType_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "notes.txt",
            "text/plain",
            "hello".getBytes()
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> fileUploadService.upload(file));
        assertEquals(400, exception.getCode());
        assertEquals("File type is not supported", exception.getMessage());
    }

    @Test
    void upload_withInvalidContentType_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "avatar.png",
            "text/plain",
            "hello".getBytes()
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> fileUploadService.upload(file));
        assertEquals(400, exception.getCode());
        assertEquals("Invalid content type", exception.getMessage());
    }

    @Test
    void upload_withPathTraversal_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "../evil.png",
            "image/png",
            "evil".getBytes()
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> fileUploadService.upload(file));
        assertEquals(400, exception.getCode());
        assertEquals("Invalid file path", exception.getMessage());
    }

    @Test
    void validateFile_withValidFile_noException() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "avatar.jpeg",
            "image/jpeg",
            "image-content".getBytes()
        );

        assertDoesNotThrow(() -> fileUploadService.validateFile(file));
    }
}
