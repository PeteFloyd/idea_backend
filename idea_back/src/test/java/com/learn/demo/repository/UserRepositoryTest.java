package com.learn.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.learn.demo.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsernameReturnsUser() {
        User user = buildUser("alice", "alice@example.com");
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("alice");
        assertTrue(found.isPresent());
        assertEquals("alice", found.get().getUsername());
    }

    @Test
    void findByUsernameReturnsEmptyWhenMissing() {
        Optional<User> found = userRepository.findByUsername("missing");
        assertTrue(found.isEmpty());
    }

    @Test
    void existsByUsernameReturnsTrueWhenPresent() {
        userRepository.save(buildUser("bob", "bob@example.com"));

        assertTrue(userRepository.existsByUsername("bob"));
        assertFalse(userRepository.existsByUsername("nope"));
    }

    @Test
    void findByEmailReturnsUserWhenPresent() {
        userRepository.save(buildUser("carl", "carl@example.com"));

        Optional<User> found = userRepository.findByEmail("carl@example.com");
        assertTrue(found.isPresent());
        assertEquals("carl", found.get().getUsername());
    }

    @Test
    void findByEmailReturnsEmptyWhenMissing() {
        Optional<User> found = userRepository.findByEmail("missing@example.com");
        assertTrue(found.isEmpty());
    }

    private User buildUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("secret123");
        user.setEmail(email);
        return user;
    }
}
