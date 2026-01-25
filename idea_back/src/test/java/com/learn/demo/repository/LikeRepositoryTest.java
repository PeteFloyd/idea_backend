package com.learn.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.learn.demo.entity.Idea;
import com.learn.demo.entity.Like;
import com.learn.demo.entity.User;
import com.learn.demo.enums.IdeaStatus;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class LikeRepositoryTest {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IdeaRepository ideaRepository;

    @Test
    void existsByUserIdAndIdeaIdReflectsPresence() {
        User user = userRepository.save(buildUser("likeuser", "like@example.com"));
        Idea idea = ideaRepository.save(buildIdea(user, "Idea", "Desc"));

        assertFalse(likeRepository.existsByUserIdAndIdeaId(user.getId(), idea.getId()));

        likeRepository.save(buildLike(user, idea));
        assertTrue(likeRepository.existsByUserIdAndIdeaId(user.getId(), idea.getId()));
    }

    @Test
    void countByIdeaIdReturnsZeroWhenNoLikes() {
        User user = userRepository.save(buildUser("countuser", "count@example.com"));
        Idea idea = ideaRepository.save(buildIdea(user, "Idea", "Desc"));

        assertEquals(0, likeRepository.countByIdeaId(idea.getId()));
    }

    @Test
    void countByIdeaIdCountsLikes() {
        User user = userRepository.save(buildUser("countuser2", "count2@example.com"));
        Idea idea = ideaRepository.save(buildIdea(user, "Idea", "Desc"));
        User other = userRepository.save(buildUser("otheruser", "other@example.com"));

        likeRepository.save(buildLike(user, idea));
        likeRepository.save(buildLike(other, idea));

        assertEquals(2, likeRepository.countByIdeaId(idea.getId()));
    }

    @Test
    void findByUserIdAndIdeaIdReturnsLike() {
        User user = userRepository.save(buildUser("finduser", "find@example.com"));
        Idea idea = ideaRepository.save(buildIdea(user, "Idea", "Desc"));
        likeRepository.save(buildLike(user, idea));

        Optional<Like> found = likeRepository.findByUserIdAndIdeaId(user.getId(), idea.getId());
        assertTrue(found.isPresent());
    }

    @Test
    void findByUserIdAndIdeaIdReturnsEmptyWhenMissing() {
        Optional<Like> found = likeRepository.findByUserIdAndIdeaId(1L, 2L);
        assertTrue(found.isEmpty());
    }

    private User buildUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("secret123");
        user.setEmail(email);
        return user;
    }

    private Idea buildIdea(User user, String title, String description) {
        Idea idea = new Idea();
        idea.setUser(user);
        idea.setTitle(title);
        idea.setDescription(description);
        idea.setStatus(IdeaStatus.ACTIVE);
        return idea;
    }

    private Like buildLike(User user, Idea idea) {
        Like like = new Like();
        like.setUser(user);
        like.setIdea(idea);
        return like;
    }
}
