package com.learn.demo.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file")
@Data
public class FileStorageConfig {
    private String uploadDir = "./uploads";
    private String baseUrl;
    private long maxFileSize = 5242880L;
    private List<String> allowedTypes = List.of("jpg", "jpeg", "png", "gif", "webp");
}
