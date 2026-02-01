package com.learn.demo.service;

import com.learn.demo.config.FileStorageConfig;
import com.learn.demo.dto.file.FileUploadResponse;
import com.learn.demo.exception.BusinessException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileUploadService {
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp"
    );

    private final FileStorageConfig fileStorageConfig;

    public FileUploadResponse upload(MultipartFile file) {
        validateFile(file);
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getExtension(originalFilename).toLowerCase();
        String storedFilename = UUID.randomUUID() + "." + extension;
        Path uploadDirPath = Paths.get(fileStorageConfig.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDirPath);
            Path targetPath = uploadDirPath.resolve(storedFilename).normalize();
            if (!targetPath.startsWith(uploadDirPath)) {
                throw new BusinessException(400, "Invalid file path");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new BusinessException(400, "Failed to store file");
        }
        return FileUploadResponse.builder()
            .url(buildFileUrl(storedFilename))
            .filename(storedFilename)
            .size(file.getSize())
            .build();
    }

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "File is empty");
        }
        if (file.getSize() > fileStorageConfig.getMaxFileSize()) {
            throw new BusinessException(400, "File size exceeds limit");
        }
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException(400, "File name is empty");
        }
        String cleanedFilename = StringUtils.cleanPath(originalFilename);
        if (cleanedFilename.contains("..")) {
            throw new BusinessException(400, "Invalid file path");
        }
        String extension = getExtension(originalFilename);
        if (!StringUtils.hasText(extension)) {
            throw new BusinessException(400, "File type is not supported");
        }
        boolean allowed = fileStorageConfig.getAllowedTypes().stream()
            .anyMatch(type -> type.equalsIgnoreCase(extension));
        if (!allowed) {
            throw new BusinessException(400, "File type is not supported");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(400, "Invalid content type");
        }
    }

    private String buildFileUrl(String storedFilename) {
        String baseUrl = fileStorageConfig.getBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            throw new BusinessException(400, "Base URL is not configured");
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/uploads/" + storedFilename;
    }

    private String getExtension(String filename) {
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            return "";
        }
        return filename.substring(index + 1);
    }
}
