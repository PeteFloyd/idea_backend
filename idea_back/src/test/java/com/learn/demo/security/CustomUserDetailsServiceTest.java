package com.learn.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.learn.demo.entity.User;
import com.learn.demo.enums.UserRole;
import com.learn.demo.enums.UserStatus;
import com.learn.demo.repository.UserRepository;
import com.learn.demo.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(CustomUserDetailsService.class)
@ActiveProfiles("test")
class CustomUserDetailsServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsernameReturnsUserPrincipal() {
        User user = buildUser("alice");
        userRepository.save(user);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("alice");

        assertEquals("alice", userDetails.getUsername());
    }

    @Test
    void loadUserByUsernameThrowsWhenMissing() {
        assertThrows(UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername("missing"));
    }

    private User buildUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("secret");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
