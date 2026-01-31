package com.learn.demo.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class TokenBlacklistServiceTest {

    @Test
    void addToBlacklistMarksTokenAsBlacklisted() {
        TokenBlacklistService service = new TokenBlacklistService();
        String token = "token-1";

        service.addToBlacklist(token, Instant.now().plusSeconds(60));

        assertTrue(service.isBlacklisted(token));
    }

    @Test
    void isBlacklistedReturnsFalseForMissingToken() {
        TokenBlacklistService service = new TokenBlacklistService();

        assertFalse(service.isBlacklisted("missing"));
    }

    @Test
    void cleanupExpiredRemovesExpiredEntries() {
        TokenBlacklistService service = new TokenBlacklistService();
        String token = "expired";

        service.addToBlacklist(token, Instant.now().minusSeconds(60));
        service.cleanupExpired();

        assertFalse(service.isBlacklisted(token));
    }

    @Test
    void addToBlacklistIgnoresNullInput() {
        TokenBlacklistService service = new TokenBlacklistService();

        service.addToBlacklist(null, Instant.now().plusSeconds(60));
        service.addToBlacklist("token", null);

        assertFalse(service.isBlacklisted("token"));
    }

    @Test
    void isBlacklistedRemovesExpiredEntry() {
        TokenBlacklistService service = new TokenBlacklistService();
        String token = "stale";

        service.addToBlacklist(token, Instant.now().minusSeconds(5));

        assertFalse(service.isBlacklisted(token));
    }
}
