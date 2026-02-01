package com.learn.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.demo.dto.PageResponse;
import com.learn.demo.dto.idea.CreateIdeaRequest;
import com.learn.demo.dto.idea.IdeaDetailResponse;
import com.learn.demo.dto.idea.IdeaListResponse;
import com.learn.demo.dto.idea.IdeaQueryRequest;
import com.learn.demo.dto.idea.UpdateIdeaRequest;
import com.learn.demo.entity.User;
import com.learn.demo.enums.UserRole;
import com.learn.demo.enums.UserStatus;
import com.learn.demo.exception.BusinessException;
import com.learn.demo.security.JwtTokenProvider;
import com.learn.demo.security.UserPrincipal;
import com.learn.demo.service.IdeaService;
import com.learn.demo.service.CustomUserDetailsService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
    "jwt.secret=testSecretKeyForUnitTestingPurposesOnly12345678901234567890",
    "jwt.expiration=86400000"
})
class IdeaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IdeaService ideaService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void listIdeasReturns200AndBindsParams() throws Exception {
        PageResponse<IdeaListResponse> response = PageResponse.<IdeaListResponse>builder()
            .content(List.of(IdeaListResponse.builder()
                .id(1L)
                .title("t")
                .description("d")
                .liked(true)
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .build()))
            .page(1)
            .size(10)
            .totalElements(1)
            .totalPages(1)
            .build();
        when(ideaService.listIdeas(any(IdeaQueryRequest.class), eq(7L))).thenReturn(response);

        mockMvc.perform(get("/api/ideas")
                .param("page", "1")
                .param("size", "10")
                .param("sort", "likeCount,asc")
                .param("keyword", "search")
                .param("tag", "java")
                .param("userId", "99")
                .with(auth(UserRole.USER, 7L)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.content[0].id").value(1));

        ArgumentCaptor<IdeaQueryRequest> captor = ArgumentCaptor.forClass(IdeaQueryRequest.class);
        verify(ideaService).listIdeas(captor.capture(), eq(7L));
        IdeaQueryRequest captured = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(1, captured.getPage());
        org.junit.jupiter.api.Assertions.assertEquals(10, captured.getSize());
        org.junit.jupiter.api.Assertions.assertEquals("likeCount,asc", captured.getSort());
        org.junit.jupiter.api.Assertions.assertEquals("search", captured.getKeyword());
        org.junit.jupiter.api.Assertions.assertEquals("java", captured.getTag());
        org.junit.jupiter.api.Assertions.assertEquals(99L, captured.getUserId());
    }

    @Test
    void listIdeasUnauthorizedReturns401() throws Exception {
        mockMvc.perform(get("/api/ideas"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getIdeaReturns200() throws Exception {
        IdeaDetailResponse response = IdeaDetailResponse.builder()
            .id(2L)
            .title("idea")
            .description("detail")
            .liked(false)
            .createdAt(LocalDateTime.of(2024, 1, 2, 0, 0))
            .build();
        when(ideaService.getIdeaDetail(2L, 7L)).thenReturn(response);

        mockMvc.perform(get("/api/ideas/2").with(auth(UserRole.USER, 7L)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(2));
    }

    @Test
    void getIdeaNotFoundReturns404() throws Exception {
        when(ideaService.getIdeaDetail(9L, 7L))
            .thenThrow(new BusinessException(404, "Idea not found"));

        mockMvc.perform(get("/api/ideas/9").with(auth(UserRole.USER, 7L)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("Idea not found"));
    }

    @Test
    void getIdeaUnauthorizedReturns401() throws Exception {
        mockMvc.perform(get("/api/ideas/2"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createIdeaReturns201() throws Exception {
        CreateIdeaRequest request = CreateIdeaRequest.builder()
            .title("title")
            .description("desc")
            .images(List.of("img"))
            .tags(List.of("tag"))
            .build();
        IdeaDetailResponse response = IdeaDetailResponse.builder()
            .id(3L)
            .title("title")
            .description("desc")
            .liked(false)
            .build();
        when(ideaService.createIdea(any(CreateIdeaRequest.class), eq(7L))).thenReturn(response);

        mockMvc.perform(post("/api/ideas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(auth(UserRole.USER, 7L)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(3));
    }

    @Test
    void createIdeaValidationReturns400() throws Exception {
        CreateIdeaRequest request = CreateIdeaRequest.builder()
            .title("")
            .description("")
            .build();

        mockMvc.perform(post("/api/ideas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(auth(UserRole.USER, 7L)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void createIdeaUnauthorizedReturns401() throws Exception {
        CreateIdeaRequest request = CreateIdeaRequest.builder()
            .title("title")
            .description("desc")
            .build();

        mockMvc.perform(post("/api/ideas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void updateIdeaReturns200() throws Exception {
        UpdateIdeaRequest request = UpdateIdeaRequest.builder()
            .title("t")
            .description("d")
            .build();
        IdeaDetailResponse response = IdeaDetailResponse.builder()
            .id(4L)
            .title("t")
            .description("d")
            .liked(true)
            .build();
        when(ideaService.updateIdea(eq(4L), any(UpdateIdeaRequest.class), eq(7L))).thenReturn(response);

        mockMvc.perform(put("/api/ideas/4")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(auth(UserRole.USER, 7L)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(4));
    }

    @Test
    void updateIdeaValidationReturns400() throws Exception {
        UpdateIdeaRequest request = UpdateIdeaRequest.builder()
            .title("")
            .description("")
            .build();

        mockMvc.perform(put("/api/ideas/4")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(auth(UserRole.USER, 7L)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void updateIdeaForbiddenReturns403() throws Exception {
        UpdateIdeaRequest request = UpdateIdeaRequest.builder()
            .title("t")
            .description("d")
            .build();
        when(ideaService.updateIdea(eq(4L), any(UpdateIdeaRequest.class), eq(7L)))
            .thenThrow(new BusinessException(403, "Forbidden"));

        mockMvc.perform(put("/api/ideas/4")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(auth(UserRole.USER, 7L)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403))
            .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void updateIdeaNotFoundReturns404() throws Exception {
        UpdateIdeaRequest request = UpdateIdeaRequest.builder()
            .title("t")
            .description("d")
            .build();
        when(ideaService.updateIdea(eq(5L), any(UpdateIdeaRequest.class), eq(7L)))
            .thenThrow(new BusinessException(404, "Idea not found"));

        mockMvc.perform(put("/api/ideas/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(auth(UserRole.USER, 7L)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("Idea not found"));
    }

    @Test
    void updateIdeaUnauthorizedReturns401() throws Exception {
        UpdateIdeaRequest request = UpdateIdeaRequest.builder()
            .title("t")
            .description("d")
            .build();

        mockMvc.perform(put("/api/ideas/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteIdeaReturns200ForAdmin() throws Exception {
        doNothing().when(ideaService).deleteIdea(6L, 8L, true);

        mockMvc.perform(delete("/api/ideas/6").with(auth(UserRole.ADMIN, 8L)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(ideaService).deleteIdea(6L, 8L, true);
    }

    @Test
    void deleteIdeaForbiddenReturns403() throws Exception {
        doThrow(new BusinessException(403, "Forbidden"))
            .when(ideaService).deleteIdea(6L, 7L, false);

        mockMvc.perform(delete("/api/ideas/6").with(auth(UserRole.USER, 7L)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403))
            .andExpect(jsonPath("$.message").value("Forbidden"));

        verify(ideaService).deleteIdea(6L, 7L, false);
    }

    @Test
    void deleteIdeaNotFoundReturns404() throws Exception {
        doThrow(new BusinessException(404, "Idea not found"))
            .when(ideaService).deleteIdea(10L, 7L, false);

        mockMvc.perform(delete("/api/ideas/10").with(auth(UserRole.USER, 7L)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("Idea not found"));
    }

    @Test
    void deleteIdeaUnauthorizedReturns401() throws Exception {
        mockMvc.perform(delete("/api/ideas/6"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyIdeasReturns200AndBindsParams() throws Exception {
        PageResponse<IdeaListResponse> response = PageResponse.<IdeaListResponse>builder()
            .content(List.of())
            .page(0)
            .size(5)
            .totalElements(0)
            .totalPages(0)
            .build();
        when(ideaService.getCurrentUserIdeas(any(IdeaQueryRequest.class), eq(7L))).thenReturn(response);

        mockMvc.perform(get("/api/users/me/ideas")
                .param("page", "0")
                .param("size", "5")
                .param("sort", "createdAt,desc")
                .param("keyword", "x")
                .param("tag", "y")
                .with(auth(UserRole.USER, 7L)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<IdeaQueryRequest> captor = ArgumentCaptor.forClass(IdeaQueryRequest.class);
        verify(ideaService).getCurrentUserIdeas(captor.capture(), eq(7L));
        IdeaQueryRequest captured = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(0, captured.getPage());
        org.junit.jupiter.api.Assertions.assertEquals(5, captured.getSize());
        org.junit.jupiter.api.Assertions.assertEquals("createdAt,desc", captured.getSort());
        org.junit.jupiter.api.Assertions.assertEquals("x", captured.getKeyword());
        org.junit.jupiter.api.Assertions.assertEquals("y", captured.getTag());
    }

    @Test
    void getMyIdeasUnauthorizedReturns401() throws Exception {
        mockMvc.perform(get("/api/users/me/ideas"))
            .andExpect(status().isUnauthorized());
    }

    private RequestPostProcessor auth(UserRole role, Long userId) {
        User user = new User();
        user.setId(userId);
        user.setUsername("user" + userId);
        user.setPassword("pass");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        UserPrincipal principal = new UserPrincipal(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.getAuthorities()
        );
        return authentication(authentication);
    }
}
