package com.grimoire.server.session;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents an authenticated session.
 */
public record Session(
        String sessionId,
        Long accountId,
        String username,
        Instant createdAt,
        Instant expiresAt
) {
    
    /**
     * Checks if the session is expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
    
    /**
     * Creates a new session for an account.
     */
    public static Session create(Long accountId, String username, int validityMinutes) {
        String sessionId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(validityMinutes * 60L);
        return new Session(sessionId, accountId, username, now, expiry);
    }
}
