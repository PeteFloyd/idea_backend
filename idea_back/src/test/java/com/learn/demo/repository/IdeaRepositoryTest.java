package com.learn.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.learn.demo.entity.Idea;
import com.learn.demo.entity.User;
import com.learn.demo.enums.IdeaStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class IdeaRepositoryTest {

    @Autowired
    private IdeaRepository ideaRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUserIdReturnsOnlyUsersIdeas() {
        User owner = userRepository.save(buildUser("owner", "owner@example.com"));
        User other = userRepository.save(buildUser("other", "other@example.com"));

        ideaRepository.save(buildIdea(owner, "Solar grid", "renewable", IdeaStatus.ACTIVE));
        ideaRepository.save(buildIdea(owner, "Green energy", "impact", IdeaStatus.HIDDEN));
        ideaRepository.save(buildIdea(other, "Other idea", "misc", IdeaStatus.ACTIVE));

        Page<Idea> page = ideaRepository.findByUserId(owner.getId(), PageRequest.of(0, 10));
        assertEquals(2, page.getTotalElements());
        assertTrue(page.getContent().stream().allMatch(idea -> idea.getUser().getId().equals(owner.getId())));
    }

    @Test
    void findByUserIdReturnsEmptyWhenMissing() {
        Page<Idea> page = ideaRepository.findByUserId(999L, PageRequest.of(0, 10));
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void findByStatusReturnsMatchingIdeas() {
        User owner = userRepository.save(buildUser("stat", "stat@example.com"));
        ideaRepository.save(buildIdea(owner, "A", "d", IdeaStatus.ACTIVE));
        ideaRepository.save(buildIdea(owner, "B", "d", IdeaStatus.HIDDEN));

        Page<Idea> active = ideaRepository.findByStatus(IdeaStatus.ACTIVE, PageRequest.of(0, 10));
        assertEquals(1, active.getTotalElements());
        assertEquals(IdeaStatus.ACTIVE, active.getContent().get(0).getStatus());
    }

    @Test
    void searchByKeywordMatchesTitleAndDescription() {
        User owner = userRepository.save(buildUser("search", "search@example.com"));
        Idea idea1 = ideaRepository.save(buildIdea(owner, "Green Energy", "impact", IdeaStatus.ACTIVE));
        Idea idea2 = ideaRepository.save(buildIdea(owner, "Future", "Solar grid", IdeaStatus.ACTIVE));

        Page<Idea> energy = ideaRepository.searchByKeyword("Energy", PageRequest.of(0, 10));
        assertEquals(1, energy.getTotalElements());
        assertTrue(energy.getContent().stream().anyMatch(idea -> idea.getId().equals(idea1.getId())));

        Page<Idea> solar = ideaRepository.searchByKeyword("Solar", PageRequest.of(0, 10));
        assertEquals(1, solar.getTotalElements());
        assertTrue(solar.getContent().stream().anyMatch(idea -> idea.getId().equals(idea2.getId())));
    }

    @Test
    void searchByKeywordReturnsEmptyWhenNoMatch() {
        User owner = userRepository.save(buildUser("nomatch", "nomatch@example.com"));
        ideaRepository.save(buildIdea(owner, "Alpha", "Beta", IdeaStatus.ACTIVE));

        Page<Idea> page = ideaRepository.searchByKeyword("Missing", PageRequest.of(0, 10));
        assertEquals(0, page.getTotalElements());
    }

    private User buildUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("secret123");
        user.setEmail(email);
        return user;
    }

    private Idea buildIdea(User user, String title, String description, IdeaStatus status) {
        Idea idea = new Idea();
        idea.setUser(user);
        idea.setTitle(title);
        idea.setDescription(description);
        idea.setStatus(status);
        return idea;
    }
}
