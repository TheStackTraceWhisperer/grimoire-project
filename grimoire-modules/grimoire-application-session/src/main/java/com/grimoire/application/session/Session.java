package com.grimoire.application.session;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an authenticated player session.
 *
 * <p>
 * Sessions are created by the {@link SessionManager} after successful
 * authentication. Each session is bound to a single account and carries an
 * expiry timestamp.
 * </p>
 *
 * @param sessionId
 *            unique session identifier
 * @param accountId
 *            the account this session belongs to
 * @param username
 *            display name at the time of creation
 * @param createdAt
 *            when the session was created
 * @param expiresAt
 *            when the session expires
 */
public record Session(
        String sessionId,
        String accountId,
        String username,
        Instant createdAt,
        Instant expiresAt) {

    // Compact constructor — validates all fields are non-null.
    public Session {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    /**
     * Checks whether this session has expired.
     *
     * @return {@code true} if the current time is after {@code expiresAt}
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Factory method that creates a new session with a generated ID.
     *
     * @param accountId
     *            the account identifier
     * @param username
     *            the player's display name
     * @param validityMinutes
     *            how many minutes the session remains valid
     * @return a new {@code Session}
     */
    public static Session create(String accountId, String username, int validityMinutes) {
        String sessionId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(validityMinutes * 60L);
        return new Session(sessionId, accountId, username, now, expiry);
    }
}
