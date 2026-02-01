package com.learn.demo.specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.learn.demo.dto.idea.IdeaQueryRequest;
import com.learn.demo.entity.Idea;
import com.learn.demo.entity.Tag;
import com.learn.demo.entity.User;
import com.learn.demo.enums.IdeaStatus;
import com.learn.demo.repository.IdeaRepository;
import com.learn.demo.repository.TagRepository;
import com.learn.demo.repository.UserRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class IdeaSpecificationsTest {

    @Autowired
    private IdeaRepository ideaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    void combineFiltersByKeywordAndExcludesDeleted() {
        User user = userRepository.save(buildUser("kwd", "kw@example.com"));
        ideaRepository.save(buildIdea(user, "Solar Grid", "clean energy", IdeaStatus.ACTIVE, Set.of()));
        ideaRepository.save(buildIdea(user, "Wind", "Solar panels", IdeaStatus.HIDDEN, Set.of()));
        ideaRepository.save(buildIdea(user, "Solar", "deleted", IdeaStatus.DELETED, Set.of()));

        IdeaQueryRequest request = IdeaQueryRequest.builder().keyword("solar").build();
        List<Idea> results = ideaRepository.findAll(IdeaSpecifications.combine(request));

        assertEquals(2, results.size());
        assertTrue(results.stream().noneMatch(idea -> idea.getStatus() == IdeaStatus.DELETED));
    }

    @Test
    void combineFiltersByTag() {
        User user = userRepository.save(buildUser("tag", "tag@example.com"));
        Tag green = tagRepository.save(buildTag("Green"));
        Tag blue = tagRepository.save(buildTag("Blue"));

        ideaRepository.save(buildIdea(user, "Idea1", "Desc", IdeaStatus.ACTIVE, Set.of(green)));
        ideaRepository.save(buildIdea(user, "Idea2", "Desc", IdeaStatus.DELETED, Set.of(green)));
        ideaRepository.save(buildIdea(user, "Idea3", "Desc", IdeaStatus.ACTIVE, Set.of(blue)));

        IdeaQueryRequest request = IdeaQueryRequest.builder().tag("green").build();
        List<Idea> results = ideaRepository.findAll(IdeaSpecifications.combine(request));

        assertEquals(1, results.size());
        assertTrue(results.stream().allMatch(idea -> idea.getTags().contains(green)));
    }

    @Test
    void combineFiltersByUserId() {
        User owner = userRepository.save(buildUser("owner", "owner@spec.com"));
        User other = userRepository.save(buildUser("other", "other@spec.com"));

        ideaRepository.save(buildIdea(owner, "Idea1", "Desc", IdeaStatus.ACTIVE, Set.of()));
        ideaRepository.save(buildIdea(owner, "Idea2", "Desc", IdeaStatus.DELETED, Set.of()));
        ideaRepository.save(buildIdea(other, "Idea3", "Desc", IdeaStatus.ACTIVE, Set.of()));

        IdeaQueryRequest request = IdeaQueryRequest.builder().userId(owner.getId()).build();
        List<Idea> results = ideaRepository.findAll(IdeaSpecifications.combine(request));

        assertEquals(1, results.size());
        assertTrue(results.stream().allMatch(idea -> idea.getUser().getId().equals(owner.getId())));
    }

    @Test
    void combineExcludesDeletedWhenNoFiltersProvided() {
        User user = userRepository.save(buildUser("plain", "plain@example.com"));
        ideaRepository.save(buildIdea(user, "Idea1", "Desc", IdeaStatus.ACTIVE, Set.of()));
        ideaRepository.save(buildIdea(user, "Idea2", "Desc", IdeaStatus.HIDDEN, Set.of()));
        ideaRepository.save(buildIdea(user, "Idea3", "Desc", IdeaStatus.DELETED, Set.of()));

        IdeaQueryRequest request = IdeaQueryRequest.builder().build();
        List<Idea> results = ideaRepository.findAll(IdeaSpecifications.combine(request));

        assertEquals(2, results.size());
        assertTrue(results.stream().noneMatch(idea -> idea.getStatus() == IdeaStatus.DELETED));
    }

    private User buildUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("secret123");
        user.setEmail(email);
        return user;
    }

    private Idea buildIdea(User user, String title, String description, IdeaStatus status, Set<Tag> tags) {
        Idea idea = new Idea();
        idea.setUser(user);
        idea.setTitle(title);
        idea.setDescription(description);
        idea.setStatus(status);
        idea.setTags(tags);
        return idea;
    }

    private Tag buildTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setUsageCount(1L);
        return tag;
    }
}
