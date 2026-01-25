package com.learn.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.learn.demo.entity.Comment;
import com.learn.demo.entity.Idea;
import com.learn.demo.entity.User;
import com.learn.demo.enums.CommentStatus;
import com.learn.demo.enums.IdeaStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IdeaRepository ideaRepository;

    @Test
    void findByIdeaIdAndStatusFiltersByStatus() {
        User user = userRepository.save(buildUser("commenter", "commenter@example.com"));
        Idea idea = ideaRepository.save(buildIdea(user, "Idea", "Desc"));

        commentRepository.save(buildComment(user, idea, "Nice", CommentStatus.ACTIVE));
        commentRepository.save(buildComment(user, idea, "Removed", CommentStatus.DELETED));

        Page<Comment> active = commentRepository.findByIdeaIdAndStatus(
            idea.getId(),
            CommentStatus.ACTIVE,
            PageRequest.of(0, 10)
        );

        assertEquals(1, active.getTotalElements());
        assertEquals(CommentStatus.ACTIVE, active.getContent().get(0).getStatus());
    }

    @Test
    void findByIdeaIdAndStatusReturnsEmptyWhenMissing() {
        Page<Comment> page = commentRepository.findByIdeaIdAndStatus(
            999L,
            CommentStatus.ACTIVE,
            PageRequest.of(0, 10)
        );
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void countByIdeaIdAndStatusCountsMatching() {
        User user = userRepository.save(buildUser("countc", "countc@example.com"));
        Idea idea = ideaRepository.save(buildIdea(user, "Idea", "Desc"));

        commentRepository.save(buildComment(user, idea, "One", CommentStatus.ACTIVE));
        commentRepository.save(buildComment(user, idea, "Two", CommentStatus.ACTIVE));
        commentRepository.save(buildComment(user, idea, "Gone", CommentStatus.DELETED));

        assertEquals(2, commentRepository.countByIdeaIdAndStatus(idea.getId(), CommentStatus.ACTIVE));
        assertEquals(1, commentRepository.countByIdeaIdAndStatus(idea.getId(), CommentStatus.DELETED));
    }

    @Test
    void countByIdeaIdAndStatusReturnsZeroWhenMissing() {
        assertEquals(0, commentRepository.countByIdeaIdAndStatus(1L, CommentStatus.ACTIVE));
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

    private Comment buildComment(User user, Idea idea, String content, CommentStatus status) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setIdea(idea);
        comment.setContent(content);
        comment.setStatus(status);
        return comment;
    }
}
