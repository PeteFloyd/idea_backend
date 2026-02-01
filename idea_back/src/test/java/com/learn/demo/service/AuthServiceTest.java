package com.learn.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.learn.demo.config.JwtConfig;
import com.learn.demo.dto.auth.LoginRequest;
import com.learn.demo.dto.auth.LoginResponse;
import com.learn.demo.dto.auth.RegisterRequest;
import com.learn.demo.entity.User;
import com.learn.demo.enums.UserRole;
import com.learn.demo.enums.UserStatus;
import com.learn.demo.exception.BusinessException;
import com.learn.demo.repository.UserRepository;
import com.learn.demo.security.JwtTokenProvider;
import com.learn.demo.security.TokenBlacklistService;
import com.learn.demo.security.UserPrincipal;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.concurrent.atomic.AtomicReference;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private JwtConfig jwtConfig;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtConfig = new JwtConfig();
        jwtConfig.setExpiration(3600000);
        authService = new AuthService(
            userRepository,
            passwordEncoder,
            authenticationManager,
            jwtTokenProvider,
            tokenBlacklistService,
            jwtConfig
        );
    }

    @Test
    void registerSuccessCreatesUserWithEncodedPassword() {
        RegisterRequest request = new RegisterRequest("alice", "pass123", "a@example.com");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        AtomicReference<User> savedRef = new AtomicReference<>();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            savedRef.set(saved);
            saved.setId(1L);
            return saved;
        });

        User result = authService.register(request);

        assertEquals(1L, result.getId());
        verify(userRepository).save(any(User.class));
        User saved = savedRef.get();
        assertEquals("alice", saved.getUsername());
        assertEquals("encoded", saved.getPassword());
        assertEquals("a@example.com", saved.getEmail());
        assertEquals(UserRole.USER, saved.getRole());
        assertEquals(UserStatus.ACTIVE, saved.getStatus());
    }

    @Test
    void registerDuplicateUsernameThrows400() {
        RegisterRequest request = new RegisterRequest("alice", "pass123", "a@example.com");
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.register(request));

        assertEquals(400, exception.getCode());
        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    void loginSuccessReturnsJwt() {
        LoginRequest request = new LoginRequest("alice", "pass123");
        User user = new User();
        user.setId(9L);
        user.setUsername("alice");
        user.setPassword("encoded");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        UserPrincipal principal = new UserPrincipal(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.getAuthorities()
        );
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(principal)).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals(9L, response.getUser().getId());
        assertEquals("alice", response.getUser().getUsername());
        assertEquals("USER", response.getUser().getRole());
    }

    @Test
    void loginWrongPasswordThrows401() {
        LoginRequest request = new LoginRequest("alice", "bad");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void loginDisabledUserThrows403() {
        LoginRequest request = new LoginRequest("alice", "pass123");
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("disabled"));

        assertThrows(DisabledException.class, () -> authService.login(request));
    }

    @Test
    void logoutAddsTokenToBlacklist() {
        Instant expiresAt = Instant.now().plusSeconds(60);
        when(jwtTokenProvider.getExpirationDateFromToken("token")).thenReturn(Date.from(expiresAt));

        AtomicReference<Instant> instantRef = new AtomicReference<>();
        doAnswer(invocation -> {
            Instant captured = invocation.getArgument(1);
            instantRef.set(captured);
            return null;
        }).when(tokenBlacklistService).addToBlacklist(any(String.class), any(Instant.class));

        authService.logout("token");

        // Date.from loses nanosecond precision, so compare at millisecond level
        assertEquals(expiresAt.toEpochMilli(), instantRef.get().toEpochMilli());
    }
}
