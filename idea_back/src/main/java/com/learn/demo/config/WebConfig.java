package com.learn.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = fileStorageConfig.getUploadDir();
        if (!uploadDir.endsWith("/")) {
            uploadDir = uploadDir + "/";
        }
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + uploadDir);
    }
}
