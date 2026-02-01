package com.learn.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.learn.demo.dto.file.FileUploadResponse;
import com.learn.demo.exception.BusinessException;
import com.learn.demo.service.FileUploadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
    "jwt.secret=testSecretKeyForUnitTestingPurposesOnly12345678901234567890",
    "jwt.expiration=86400000",
    "file.upload-dir=./test-uploads",
    "file.base-url=http://localhost:8080"
})
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileUploadService fileUploadService;

    @Test
    void upload_withValidFile_returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "avatar.png",
            "image/png",
            "image-content".getBytes()
        );

        FileUploadResponse response = FileUploadResponse.builder()
            .url("http://localhost:8080/uploads/abc.png")
            .filename("abc.png")
            .size(file.getSize())
            .build();

        when(fileUploadService.upload(any())).thenReturn(response);

        mockMvc.perform(multipart("/api/files/upload").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data.url").value("http://localhost:8080/uploads/abc.png"))
            .andExpect(jsonPath("$.data.filename").value("abc.png"));
    }

    @Test
    void upload_withInvalidFile_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.png",
            "image/png",
            new byte[0]
        );

        when(fileUploadService.upload(any())).thenThrow(new BusinessException(400, "File is empty"));

        mockMvc.perform(multipart("/api/files/upload").file(file))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("File is empty"));
    }
}
