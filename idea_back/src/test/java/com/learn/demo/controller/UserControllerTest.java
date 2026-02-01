package com.learn.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.demo.dto.user.ChangePasswordRequest;
import com.learn.demo.dto.user.UpdateUserRequest;
import com.learn.demo.entity.User;
import com.learn.demo.enums.UserRole;
import com.learn.demo.enums.UserStatus;
import com.learn.demo.security.UserPrincipal;
import com.learn.demo.security.JwtTokenProvider;
import com.learn.demo.service.CustomUserDetailsService;
import com.learn.demo.service.UserService;
import com.learn.demo.config.FileStorageConfig;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@Import(UserControllerTest.TestConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "alice")
    void getCurrentUserReturns200() throws Exception {
        User user = buildUser("alice");
        when(userService.getCurrentUser("alice")).thenReturn(user);

        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.username").value("alice"))
            .andExpect(jsonPath("$.data.email").value("alice@example.com"))
            .andExpect(jsonPath("$.data.role").value("USER"))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"))
            .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @WithMockUser(username = "alice")
    void updateUserReturns200() throws Exception {
        User updated = buildUser("alice");
        updated.setEmail("new@example.com");
        updated.setAvatar("new-avatar.png");
        when(userService.updateUser(any(String.class), any(UpdateUserRequest.class))).thenReturn(updated);

        UpdateUserRequest request = new UpdateUserRequest("new@example.com", "new-avatar.png");

        mockMvc.perform(put("/api/users/me")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data.email").value("new@example.com"))
            .andExpect(jsonPath("$.data.avatar").value("new-avatar.png"));
    }

    @Test
    @WithMockUser(username = "alice")
    void updateUserReturns400ForInvalidEmail() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("bad-email", "avatar.png");

        mockMvc.perform(put("/api/users/me")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors[0].field").value("email"));
    }

    @Test
    @WithMockUser(username = "alice")
    void changePasswordReturns200() throws Exception {
        doNothing().when(userService).changePassword(any(String.class), any(ChangePasswordRequest.class));

        ChangePasswordRequest request = new ChangePasswordRequest("oldPass123", "newPass123");

        mockMvc.perform(put("/api/users/me/password")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    @WithMockUser(username = "alice")
    void changePasswordReturns400ForShortPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass123", "123");

        mockMvc.perform(put("/api/users/me/password")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors[0].field").value("newPassword"));
    }

    private User buildUser(String username) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail("alice@example.com");
        user.setAvatar("avatar.png");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
        return user;
    }

    static class TestConfig implements WebMvcConfigurer {
        @org.springframework.context.annotation.Bean
        public FileStorageConfig fileStorageConfig() {
            FileStorageConfig config = new FileStorageConfig();
            config.setUploadDir("./uploads");
            return config;
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                    return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                        && UserPrincipal.class.isAssignableFrom(parameter.getParameterType());
                }

                @Override
                public Object resolveArgument(
                    org.springframework.core.MethodParameter parameter,
                    ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest,
                    WebDataBinderFactory binderFactory
                ) {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication == null) {
                        return null;
                    }
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof UserPrincipal userPrincipal && userPrincipal.getUser() != null) {
                        return userPrincipal;
                    }
                    User user = new User();
                    user.setUsername(authentication.getName());
                    return new UserPrincipal(user);
                }
            });
        }
    }
}
