package com.learn.demo.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.demo.dto.idea.CreateIdeaRequest;
import com.learn.demo.dto.idea.UpdateIdeaRequest;
import com.learn.demo.entity.Idea;
import com.learn.demo.entity.Like;
import com.learn.demo.entity.Tag;
import com.learn.demo.entity.User;
import com.learn.demo.enums.IdeaStatus;
import com.learn.demo.enums.UserRole;
import com.learn.demo.enums.UserStatus;
import com.learn.demo.repository.IdeaRepository;
import com.learn.demo.repository.LikeRepository;
import com.learn.demo.repository.TagRepository;
import com.learn.demo.repository.UserRepository;
import com.learn.demo.security.JwtTokenProvider;
import com.learn.demo.security.UserPrincipal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class IdeaCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdeaRepository ideaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void fullCrudWorkflowUpdatesTagsAndSnippet() throws Exception {
        User author = saveUser("author", UserRole.USER);
        String longDescription = "x".repeat(220);
        CreateIdeaRequest createRequest = CreateIdeaRequest.builder()
            .title("First Idea")
            .description(longDescription)
            .images(List.of("img1"))
            .tags(List.of("Spring", "Java"))
            .build();

        MvcResult createResult = mockMvc.perform(post("/api/ideas")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearer(author))
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn();
        Long ideaId = readJson(createResult).path("data").path("id").asLong();

        MvcResult listResult = mockMvc.perform(get("/api/ideas")
                .header("Authorization", bearer(author)))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode listContent = readJson(listResult).path("data").path("content");
        assertEquals(1, listContent.size());
        String snippet = listContent.get(0).path("description").asText();
        assertEquals(200, snippet.length());

        String updatedDescription = "y".repeat(260);
        UpdateIdeaRequest updateRequest = UpdateIdeaRequest.builder()
            .title("Updated")
            .description(updatedDescription)
            .images(List.of("img2"))
            .tags(List.of("Java", "DB"))
            .build();
        mockMvc.perform(put("/api/ideas/" + ideaId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearer(author))
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk());

        Tag spring = tagRepository.findByNameIgnoreCase("Spring").orElseThrow();
        Tag java = tagRepository.findByNameIgnoreCase("Java").orElseThrow();
        Tag db = tagRepository.findByNameIgnoreCase("DB").orElseThrow();
        assertEquals(0L, spring.getUsageCount());
        assertEquals(1L, java.getUsageCount());
        assertEquals(1L, db.getUsageCount());

        MvcResult detailResult = mockMvc.perform(get("/api/ideas/" + ideaId)
                .header("Authorization", bearer(author)))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode detail = readJson(detailResult).path("data");
        assertEquals(updatedDescription, detail.path("description").asText());
        Set<String> detailTags = toStringSet(detail.path("tags"));
        assertTrue(detailTags.contains("Java"));
        assertTrue(detailTags.contains("DB"));

        mockMvc.perform(delete("/api/ideas/" + ideaId)
                .header("Authorization", bearer(author)))
            .andExpect(status().isOk());

        MvcResult afterDelete = mockMvc.perform(get("/api/ideas")
                .header("Authorization", bearer(author)))
            .andExpect(status().isOk())
            .andReturn();
        assertEquals(0, readJson(afterDelete).path("data").path("totalElements").asLong());

        Idea deleted = ideaRepository.findById(ideaId).orElseThrow();
        assertEquals(IdeaStatus.DELETED, deleted.getStatus());
        Tag javaAfterDelete = tagRepository.findByNameIgnoreCase("Java").orElseThrow();
        Tag dbAfterDelete = tagRepository.findByNameIgnoreCase("DB").orElseThrow();
        assertEquals(0L, javaAfterDelete.getUsageCount());
        assertEquals(0L, dbAfterDelete.getUsageCount());
    }

    @Test
    void permissionEnforcementBlocksNonOwnerAllowsAdmin() throws Exception {
        User author = saveUser("owner", UserRole.USER);
        User other = saveUser("other", UserRole.USER);
        User admin = saveUser("admin", UserRole.ADMIN);
        Idea idea = saveIdea(author, "Idea", "Desc", Set.of());

        UpdateIdeaRequest updateRequest = UpdateIdeaRequest.builder()
            .title("New")
            .description("New Desc")
            .build();

        mockMvc.perform(put("/api/ideas/" + idea.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearer(other))
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403))
            .andExpect(jsonPath("$.message").value("Forbidden"));

        mockMvc.perform(delete("/api/ideas/" + idea.getId())
                .header("Authorization", bearer(other)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403))
            .andExpect(jsonPath("$.message").value("Forbidden"));

        mockMvc.perform(delete("/api/ideas/" + idea.getId())
                .header("Authorization", bearer(admin)))
            .andExpect(status().isOk());

        Idea deleted = ideaRepository.findById(idea.getId()).orElseThrow();
        assertEquals(IdeaStatus.DELETED, deleted.getStatus());
    }

    @Test
    void paginationAndSortingWorkAcrossPages() throws Exception {
        User user = saveUser("pager", UserRole.USER);

        MvcResult emptyResult = mockMvc.perform(get("/api/ideas")
                .header("Authorization", bearer(user)))
            .andExpect(status().isOk())
            .andReturn();
        assertEquals(0, readJson(emptyResult).path("data").path("totalElements").asLong());

        for (int i = 0; i < 25; i++) {
            Idea idea = saveIdea(user, "Idea " + i, "Desc " + i, Set.of());
            idea.setLikeCount((long) i);
            ideaRepository.save(idea);
        }

        MvcResult page0 = mockMvc.perform(get("/api/ideas")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", bearer(user)))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode page0Data = readJson(page0).path("data");
        assertEquals(25, page0Data.path("totalElements").asLong());
        assertEquals(3, page0Data.path("totalPages").asInt());
        assertEquals(10, page0Data.path("content").size());

        MvcResult page2 = mockMvc.perform(get("/api/ideas")
                .param("page", "2")
                .param("size", "10")
                .header("Authorization", bearer(user)))
            .andExpect(status().isOk())
            .andReturn();
        assertEquals(5, readJson(page2).path("data").path("content").size());

        MvcResult likeSorted = mockMvc.perform(get("/api/ideas")
                .param("sort", "likeCount,asc")
                .param("size", "5")
                .header("Authorization", bearer(user)))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode likeContent = readJson(likeSorted).path("data").path("content");
        long firstLike = likeContent.get(0).path("likeCount").asLong();
        long lastLike = likeContent.get(likeContent.size() - 1).path("likeCount").asLong();
        assertTrue(firstLike <= lastLike);

        MvcResult createdAtSorted = mockMvc.perform(get("/api/ideas")
                .param("sort", "createdAt,asc")
                .param("size", "5")
                .header("Authorization", bearer(user)))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode createdContent = readJson(createdAtSorted).path("data").path("content");
        LocalDateTime firstCreated = LocalDateTime.parse(createdContent.get(0).path("createdAt").asText());
        LocalDateTime lastCreated = LocalDateTime.parse(createdContent.get(createdContent.size() - 1)
            .path("createdAt").asText());
        assertTrue(!firstCreated.isAfter(lastCreated));
    }

    @Test
    void filteringByKeywordTagAndUserIdWorks() throws Exception {
        User user1 = saveUser("filter1", UserRole.USER);
        User user2 = saveUser("filter2", UserRole.USER);
        Tag java = saveTag("java", 2L);
        Tag spring = saveTag("spring", 1L);

        Idea alpha = saveIdea(user1, "Alpha Idea", "Something", Set.of(java));
        Idea beta = saveIdea(user1, "Other", "Contains Beta", Set.of(spring));
        Idea gamma = saveIdea(user2, "Gamma", "Another", Set.of(java));

        MvcResult keywordTitle = mockMvc.perform(get("/api/ideas")
                .param("keyword", "alpha")
                .header("Authorization", bearer(user1)))
            .andExpect(status().isOk())
            .andReturn();
        List<Long> titleIds = toIdList(readJson(keywordTitle).path("data").path("content"));
        assertEquals(1, titleIds.size());
        assertTrue(titleIds.contains(alpha.getId()));

        MvcResult keywordDesc = mockMvc.perform(get("/api/ideas")
                .param("keyword", "beta")
                .header("Authorization", bearer(user1)))
            .andExpect(status().isOk())
            .andReturn();
        List<Long> descIds = toIdList(readJson(keywordDesc).path("data").path("content"));
        assertEquals(1, descIds.size());
        assertTrue(descIds.contains(beta.getId()));

        MvcResult tagFilter = mockMvc.perform(get("/api/ideas")
                .param("tag", "java")
                .header("Authorization", bearer(user1)))
            .andExpect(status().isOk())
            .andReturn();
        List<Long> tagIds = toIdList(readJson(tagFilter).path("data").path("content"));
        assertEquals(2, tagIds.size());
        assertTrue(tagIds.contains(alpha.getId()));
        assertTrue(tagIds.contains(gamma.getId()));

        MvcResult userFilter = mockMvc.perform(get("/api/ideas")
                .param("userId", user1.getId().toString())
                .header("Authorization", bearer(user1)))
            .andExpect(status().isOk())
            .andReturn();
        List<Long> userIds = toIdList(readJson(userFilter).path("data").path("content"));
        assertEquals(2, userIds.size());
        assertTrue(userIds.contains(alpha.getId()));
        assertTrue(userIds.contains(beta.getId()));
    }

    @Test
    void likedStatusReflectsUserLikes() throws Exception {
        User user = saveUser("liker", UserRole.USER);
        Idea idea = saveIdea(user, "Idea", "Desc", Set.of());

        MvcResult noLike = mockMvc.perform(get("/api/ideas")
                .header("Authorization", bearer(user)))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode noLikeContent = readJson(noLike).path("data").path("content");
        assertFalse(noLikeContent.get(0).path("liked").asBoolean());

        Like like = new Like();
        like.setUser(user);
        like.setIdea(idea);
        likeRepository.save(like);

        MvcResult withLike = mockMvc.perform(get("/api/ideas")
                .header("Authorization", bearer(user)))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode withLikeContent = readJson(withLike).path("data").path("content");
        assertTrue(withLikeContent.get(0).path("liked").asBoolean());
    }

    private User saveUser(String username, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("password");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private String bearer(User user) {
        return "Bearer " + jwtTokenProvider.generateToken(new UserPrincipal(user));
    }

    private Idea saveIdea(User user, String title, String description, Set<Tag> tags) {
        Idea idea = new Idea();
        idea.setUser(user);
        idea.setTitle(title);
        idea.setDescription(description);
        idea.setImages(List.of());
        idea.setStatus(IdeaStatus.ACTIVE);
        idea.setTags(new HashSet<>(tags));
        idea.setLikeCount(0L);
        idea.setCommentCount(0L);
        return ideaRepository.save(idea);
    }

    private Tag saveTag(String name, long usageCount) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setUsageCount(usageCount);
        return tagRepository.save(tag);
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private List<Long> toIdList(JsonNode content) {
        List<Long> ids = new ArrayList<>();
        for (JsonNode node : content) {
            ids.add(node.path("id").asLong());
        }
        return ids;
    }

    private Set<String> toStringSet(JsonNode array) {
        return streamArray(array).collect(Collectors.toSet());
    }

    private java.util.stream.Stream<String> streamArray(JsonNode array) {
        List<String> values = new ArrayList<>();
        for (JsonNode node : array) {
            values.add(node.asText());
        }
        return values.stream();
    }
}
