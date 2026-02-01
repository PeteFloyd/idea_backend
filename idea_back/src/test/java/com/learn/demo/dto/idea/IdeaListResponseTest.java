package com.learn.demo.dto.idea;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.learn.demo.entity.Idea;
import com.learn.demo.entity.Tag;
import com.learn.demo.entity.User;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IdeaListResponseTest {

    @Test
    void descriptionSnippetUsesFullTextWhenShorterThanLimit() {
        Idea idea = buildIdea(repeat("a", 199));
        IdeaListResponse response = IdeaListResponse.fromIdea(idea, false);
        assertEquals(199, response.getDescription().length());
    }

    @Test
    void descriptionSnippetUsesFullTextWhenAtLimit() {
        Idea idea = buildIdea(repeat("a", 200));
        IdeaListResponse response = IdeaListResponse.fromIdea(idea, false);
        assertEquals(200, response.getDescription().length());
    }

    @Test
    void descriptionSnippetTruncatesWhenOverLimit() {
        Idea idea = buildIdea(repeat("a", 201));
        IdeaListResponse response = IdeaListResponse.fromIdea(idea, true);
        assertEquals(200, response.getDescription().length());
        assertTrue(response.isLiked());
    }

    @Test
    void nullImagesAndTagsReturnEmptyLists() {
        Idea idea = buildIdea("desc");
        idea.setImages(null);
        idea.setTags(null);

        IdeaListResponse response = IdeaListResponse.fromIdea(idea, false);
        assertNotNull(response.getImages());
        assertNotNull(response.getTags());
        assertTrue(response.getImages().isEmpty());
        assertTrue(response.getTags().isEmpty());
    }

    @Test
    void authorAndCountsMappedCorrectly() {
        Idea idea = buildIdea("desc");
        idea.setLikeCount(12L);
        idea.setCommentCount(3L);

        IdeaListResponse response = IdeaListResponse.fromIdea(idea, false);
        assertEquals(12L, response.getLikeCount());
        assertEquals(3L, response.getCommentCount());
        assertEquals(1L, response.getAuthor().getId());
        assertEquals("user1", response.getAuthor().getUsername());
        assertEquals("avatar.png", response.getAuthor().getAvatar());
        assertTrue(response.getTags().containsAll(List.of("tag1", "tag2")));
        assertEquals(2, response.getTags().size());
    }

    private static Idea buildIdea(String description) {
        Idea idea = new Idea();
        idea.setId(1L);
        idea.setTitle("title");
        idea.setDescription(description);
        idea.setImages(List.of("img1"));
        idea.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
        idea.setLikeCount(0L);
        idea.setCommentCount(0L);

        User user = new User();
        user.setId(1L);
        user.setUsername("user1");
        user.setAvatar("avatar.png");
        idea.setUser(user);

        Tag tag1 = new Tag();
        tag1.setName("tag1");
        Tag tag2 = new Tag();
        tag2.setName("tag2");
        Set<Tag> tags = new HashSet<>();
        tags.add(tag1);
        tags.add(tag2);
        idea.setTags(tags);
        return idea;
    }

    private static String repeat(String value, int count) {
        return value.repeat(count);
    }
}
