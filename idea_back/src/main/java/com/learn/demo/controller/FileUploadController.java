package com.learn.demo.controller;

import com.learn.demo.dto.ApiResponse;
import com.learn.demo.dto.file.FileUploadResponse;
import com.learn.demo.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {
    private final FileUploadService fileUploadService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponse>> upload(@RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileUploadService.upload(file);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
