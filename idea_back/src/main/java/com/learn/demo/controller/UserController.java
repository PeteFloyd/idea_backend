package com.learn.demo.controller;

import com.learn.demo.dto.ApiResponse;
import com.learn.demo.dto.user.ChangePasswordRequest;
import com.learn.demo.dto.user.UpdateUserRequest;
import com.learn.demo.dto.user.UserResponse;
import com.learn.demo.entity.User;
import com.learn.demo.security.UserPrincipal;
import com.learn.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getCurrentUser(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(UserResponse.fromUser(user)));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        User user = userService.updateUser(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(UserResponse.fromUser(user)));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().code(200).message("Password changed successfully").build());
    }
}
