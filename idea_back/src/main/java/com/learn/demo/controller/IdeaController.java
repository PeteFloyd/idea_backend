package com.learn.demo.controller;

import com.learn.demo.dto.ApiResponse;
import com.learn.demo.dto.PageResponse;
import com.learn.demo.dto.idea.CreateIdeaRequest;
import com.learn.demo.dto.idea.IdeaDetailResponse;
import com.learn.demo.dto.idea.IdeaListResponse;
import com.learn.demo.dto.idea.IdeaQueryRequest;
import com.learn.demo.dto.idea.UpdateIdeaRequest;
import com.learn.demo.enums.UserRole;
import com.learn.demo.security.UserPrincipal;
import com.learn.demo.service.IdeaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class IdeaController {
    private final IdeaService ideaService;

    @GetMapping("/ideas")
    public ResponseEntity<ApiResponse<PageResponse<IdeaListResponse>>> listIdeas(
            @ModelAttribute IdeaQueryRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(
            ideaService.listIdeas(request, user.getId())));
    }

    @GetMapping("/ideas/{id}")
    public ResponseEntity<ApiResponse<IdeaDetailResponse>> getIdea(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(
            ideaService.getIdeaDetail(id, user.getId())));
    }

    @PostMapping("/ideas")
    public ResponseEntity<ApiResponse<IdeaDetailResponse>> createIdea(
            @Valid @RequestBody CreateIdeaRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
            ideaService.createIdea(request, user.getId())));
    }

    @PutMapping("/ideas/{id}")
    public ResponseEntity<ApiResponse<IdeaDetailResponse>> updateIdea(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIdeaRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(
            ideaService.updateIdea(id, request, user.getId())));
    }

    @DeleteMapping("/ideas/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteIdea(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        ideaService.deleteIdea(id, user.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/users/me/ideas")
    public ResponseEntity<ApiResponse<PageResponse<IdeaListResponse>>> getMyIdeas(
            @ModelAttribute IdeaQueryRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(
            ideaService.getCurrentUserIdeas(request, user.getId())));
    }
}
