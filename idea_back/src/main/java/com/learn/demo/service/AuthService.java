package com.learn.demo.service;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtConfig jwtConfig;

    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(400, "Username already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        String token = jwtTokenProvider.generateToken(principal);
        return LoginResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(jwtConfig.getExpiration() / 1000)
            .user(LoginResponse.UserInfo.builder()
                .id(principal.getId())
                .username(principal.getUsername())
                .role(principal.getRole().name())
                .build())
            .build();
    }

    public void logout(String token) {
        Instant expiresAt = jwtTokenProvider.getExpirationDateFromToken(token).toInstant();
        tokenBlacklistService.addToBlacklist(token, expiresAt);
    }
}
