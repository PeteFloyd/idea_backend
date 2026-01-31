package com.learn.demo.security;

import com.learn.demo.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private static final int MIN_SECRET_BYTES = 32;

    private final JwtConfig jwtConfig;
    private final TokenBlacklistService tokenBlacklistService;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtConfig jwtConfig, TokenBlacklistService tokenBlacklistService) {
        this.jwtConfig = jwtConfig;
        this.tokenBlacklistService = tokenBlacklistService;
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalArgumentException("JWT secret must be at least 256 bits");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserPrincipal userPrincipal) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtConfig.getExpiration());
        return Jwts.builder()
            .subject(userPrincipal.getUsername())
            .claim("id", userPrincipal.getId())
            .claim("role", userPrincipal.getRole().name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                return false;
            }
            return !tokenBlacklistService.isBlacklisted(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getIdFromToken(String token) {
        Object id = parseClaims(token).get("id");
        if (id instanceof Number number) {
            return number.longValue();
        }
        if (id instanceof String text) {
            return Long.valueOf(text);
        }
        return null;
    }

    public Date getExpirationDateFromToken(String token) {
        return parseClaims(token).getExpiration();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
