package com.learn.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.learn.demo.dto.user.ChangePasswordRequest;
import com.learn.demo.dto.user.UpdateUserRequest;
import com.learn.demo.entity.User;
import com.learn.demo.enums.UserRole;
import com.learn.demo.enums.UserStatus;
import com.learn.demo.exception.BusinessException;
import com.learn.demo.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void getCurrentUserReturnsUser() {
        User user = buildUser();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        User result = userService.getCurrentUser("alice");

        assertSame(user, result);
    }

    @Test
    void getCurrentUserNotFoundThrows404() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.getCurrentUser("missing"));

        assertEquals(404, exception.getCode());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void updateUserUpdatesEmailAndAvatar() {
        User user = buildUser();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest("new@example.com", "new.png");

        User result = userService.updateUser("alice", request);

        assertEquals("new@example.com", result.getEmail());
        assertEquals("new.png", result.getAvatar());
        assertEquals("alice", result.getUsername());
        assertEquals("hashed", result.getPassword());
        assertEquals(UserRole.USER, result.getRole());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
    }

    @Test
    void updateUserOnlyEmailDoesNotChangeAvatar() {
        User user = buildUser();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest("new@example.com", null);

        User result = userService.updateUser("alice", request);

        assertEquals("new@example.com", result.getEmail());
        assertEquals("old.png", result.getAvatar());
    }

    @Test
    void updateUserOnlyAvatarDoesNotChangeEmail() {
        User user = buildUser();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest(null, "new.png");

        User result = userService.updateUser("alice", request);

        assertEquals("a@example.com", result.getEmail());
        assertEquals("new.png", result.getAvatar());
    }

    @Test
    void updateUserNotFoundThrows404() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.updateUser("missing", new UpdateUserRequest()));

        assertEquals(404, exception.getCode());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void changePasswordOldPasswordMismatchThrows400() {
        User user = buildUser();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "hashed")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () ->
            userService.changePassword("alice", new ChangePasswordRequest("old", "newpass"))
        );

        assertEquals(400, exception.getCode());
        assertEquals("Old password incorrect", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePasswordSuccessUpdatesPasswordAndTimestamp() {
        User user = buildUser();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("encoded-new");
        AtomicReference<User> savedRef = new AtomicReference<>();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            savedRef.set(saved);
            return saved;
        });

        LocalDateTime before = LocalDateTime.now();
        userService.changePassword("alice", new ChangePasswordRequest("old", "newpass"));
        LocalDateTime after = LocalDateTime.now();
        User saved = savedRef.get();
        assertEquals("encoded-new", saved.getPassword());
        assertNotNull(saved.getPasswordChangedAt());
        boolean inRange = !saved.getPasswordChangedAt().isBefore(before) && !saved.getPasswordChangedAt().isAfter(after);
        assertTrue(inRange);
    }

    @Test
    void changePasswordUserNotFoundThrows404() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
            userService.changePassword("missing", new ChangePasswordRequest("old", "newpass"))
        );

        assertEquals(404, exception.getCode());
        assertEquals("User not found", exception.getMessage());
    }

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("hashed");
        user.setEmail("a@example.com");
        user.setAvatar("old.png");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
