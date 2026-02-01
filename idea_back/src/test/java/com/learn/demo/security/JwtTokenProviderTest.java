package com.learn.demo.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.learn.demo.config.JwtConfig;
import com.learn.demo.entity.User;
import com.learn.demo.enums.UserRole;
import com.learn.demo.enums.UserStatus;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    @Test
    void generateTokenProducesValidToken() {
        JwtTokenProvider provider = buildProvider("0123456789abcdef0123456789abcdef", 3600000);
        UserPrincipal principal = new UserPrincipal(buildUser("alice"));

        String token = provider.generateToken(principal);

        assertNotNull(token);
        assertTrue(provider.validateToken(token));
    }

    @Test
    void generateTokenIncludesUsernameIdAndExpiration() {
        JwtTokenProvider provider = buildProvider("0123456789abcdef0123456789abcdef", 3600000);
        UserPrincipal principal = new UserPrincipal(buildUser("alice"));

        String token = provider.generateToken(principal);

        assertEquals("alice", provider.getUsernameFromToken(token));
        assertEquals(1L, provider.getIdFromToken(token));
        assertNotNull(provider.getExpirationDateFromToken(token));
        assertNotNull(provider.getIssuedAtFromToken(token));
    }

    @Test
    void validateTokenFailsWhenExpired() {
        JwtTokenProvider provider = buildProvider("0123456789abcdef0123456789abcdef", -1000);
        UserPrincipal principal = new UserPrincipal(buildUser("bob"));

        String token = provider.generateToken(principal);

        assertFalse(provider.validateToken(token));
    }

    @Test
    void validateTokenFailsWhenBlacklisted() {
        TokenBlacklistService blacklistService = new TokenBlacklistService();
        JwtTokenProvider provider = buildProvider("0123456789abcdef0123456789abcdef", 3600000, blacklistService);
        UserPrincipal principal = new UserPrincipal(buildUser("dave"));

        String token = provider.generateToken(principal);

        blacklistService.addToBlacklist(token, provider.getExpirationDateFromToken(token).toInstant());

        assertFalse(provider.validateToken(token));
    }

    @Test
    void validateTokenFailsWithInvalidSignature() {
        JwtTokenProvider issuer = buildProvider("0123456789abcdef0123456789abcdef", 3600000);
        JwtTokenProvider validator = buildProvider("abcdef0123456789abcdef0123456789", 3600000);
        UserPrincipal principal = new UserPrincipal(buildUser("carol"));

        String token = issuer.generateToken(principal);

        assertFalse(validator.validateToken(token));
    }

    @Test
    void getIdFromTokenHandlesStringId() {
        String secret = "0123456789abcdef0123456789abcdef";
        JwtTokenProvider provider = buildProvider(secret, 3600000);
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        String token = Jwts.builder()
            .subject("erin")
            .claim("id", "12")
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(60)))
            .signWith(key, Jwts.SIG.HS256)
            .compact();

        assertEquals(12L, provider.getIdFromToken(token));
    }

    private JwtTokenProvider buildProvider(String secret, long expiration) {
        return buildProvider(secret, expiration, new TokenBlacklistService());
    }

    private JwtTokenProvider buildProvider(String secret, long expiration, TokenBlacklistService blacklistService) {
        JwtConfig config = new JwtConfig();
        config.setSecret(secret);
        config.setExpiration(expiration);
        return new JwtTokenProvider(config, blacklistService);
    }

    private User buildUser(String username) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword("secret");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
