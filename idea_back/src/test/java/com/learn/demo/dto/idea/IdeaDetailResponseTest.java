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

class IdeaDetailResponseTest {

    @Test
    void descriptionPreservesFullText() {
        String description = repeat("a", 300);
        Idea idea = buildIdea(description);

        IdeaDetailResponse response = IdeaDetailResponse.fromIdea(idea, false);
        assertEquals(300, response.getDescription().length());
    }

    @Test
    void nullImagesAndTagsReturnEmptyLists() {
        Idea idea = buildIdea("desc");
        idea.setImages(null);
        idea.setTags(null);

        IdeaDetailResponse response = IdeaDetailResponse.fromIdea(idea, false);
        assertNotNull(response.getImages());
        assertNotNull(response.getTags());
        assertTrue(response.getImages().isEmpty());
        assertTrue(response.getTags().isEmpty());
    }

    @Test
    void authorAndCountsMappedCorrectly() {
        Idea idea = buildIdea("desc");
        idea.setLikeCount(7L);
        idea.setCommentCount(2L);

        IdeaDetailResponse response = IdeaDetailResponse.fromIdea(idea, true);
        assertEquals(7L, response.getLikeCount());
        assertEquals(2L, response.getCommentCount());
        assertEquals(1L, response.getAuthor().getId());
        assertEquals("user1", response.getAuthor().getUsername());
        assertEquals("avatar.png", response.getAuthor().getAvatar());
        assertTrue(response.isLiked());
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
        Set<Tag> tags = new HashSet<>();
        tags.add(tag1);
        idea.setTags(tags);
        return idea;
    }

    private static String repeat(String value, int count) {
        return value.repeat(count);
    }
}
