package com.grimoire.application.session;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player sessions with validation and expiration.
 *
 * <p>
 * Sessions are stored in-memory using concurrent maps. Each account may have at
 * most one active session — creating a new session automatically invalidates
 * any existing session for the same account.
 * </p>
 *
 * <p>
 * Expired session cleanup is exposed via {@link #cleanupExpiredSessions()} and
 * should be called periodically by the infrastructure layer (e.g., a scheduled
 * task). This keeps scheduling concerns out of the application layer.
 * </p>
 *
 * <p>
 * <strong>Thread-safety:</strong> All public methods are safe for concurrent
 * access. Register as a singleton at the assembly layer.
 * </p>
 */
public class SessionManager {

    /** Logger for session lifecycle events. */
    private static final System.Logger LOG = System.getLogger(SessionManager.class.getName());

    /** Active sessions keyed by session ID. */
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    /** Reverse index: accountId → sessionId. */
    private final Map<String, String> accountToSession = new ConcurrentHashMap<>();

    /** How many minutes each new session remains valid. */
    private final int sessionValidityMinutes;

    /**
     * Creates a session manager with the given configuration.
     *
     * @param config
     *            session policy configuration
     */
    public SessionManager(SessionConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.sessionValidityMinutes = config.sessionValidityMinutes();
    }

    /**
     * Creates a new session for an account.
     *
     * <p>
     * If the account already has an active session, it is invalidated first.
     * </p>
     *
     * @param accountId
     *            the account identifier
     * @param username
     *            the player's display name
     * @return the newly created session
     */
    public Session createSession(String accountId, String username) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(username, "username must not be null");

        invalidateSessionForAccount(accountId);

        Session session = Session.create(accountId, username, sessionValidityMinutes);
        sessions.put(session.sessionId(), session);
        accountToSession.put(accountId, session.sessionId());

        LOG.log(System.Logger.Level.INFO,
                "Created session {0} for account {1}", session.sessionId(), username);
        return session;
    }

    /**
     * Validates a session and returns it if it is still active.
     *
     * <p>
     * If the session exists but has expired, it is automatically invalidated.
     * </p>
     *
     * @param sessionId
     *            the session identifier
     * @return the session if valid, or empty
     */
    public Optional<Session> validateSession(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null && !session.isExpired()) {
            return Optional.of(session);
        }
        if (session != null) {
            invalidateSession(sessionId);
        }
        return Optional.empty();
    }

    /**
     * Invalidates a session by session ID.
     *
     * @param sessionId
     *            the session identifier
     */
    public void invalidateSession(String sessionId) {
        Session session = sessions.remove(sessionId);
        if (session != null) {
            accountToSession.remove(session.accountId());
            LOG.log(System.Logger.Level.INFO,
                    "Invalidated session {0} for account {1}", sessionId, session.username());
        }
    }

    /**
     * Invalidates any active session for the given account.
     *
     * @param accountId
     *            the account identifier
     */
    public void invalidateSessionForAccount(String accountId) {
        String sessionId = accountToSession.remove(accountId);
        if (sessionId != null) {
            sessions.remove(sessionId);
            LOG.log(System.Logger.Level.INFO,
                    "Invalidated session for account ID {0}", accountId);
        }
    }

    /**
     * Gets the session for an account if it exists and is still valid.
     *
     * @param accountId
     *            the account identifier
     * @return the session if valid, or empty
     */
    public Optional<Session> getSessionForAccount(String accountId) {
        String sessionId = accountToSession.get(accountId);
        if (sessionId != null) {
            return validateSession(sessionId);
        }
        return Optional.empty();
    }

    /**
     * Returns the number of sessions currently stored (including possibly expired
     * ones that have not yet been cleaned up).
     *
     * @return session count
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }

    /**
     * Removes all expired sessions.
     *
     * <p>
     * This method is intended to be called periodically by the infrastructure
     * layer.
     * </p>
     *
     * @return the number of sessions removed
     */
    public int cleanupExpiredSessions() {
        Instant now = Instant.now();
        int cleaned = 0;

        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            if (now.isAfter(entry.getValue().expiresAt())) {
                invalidateSession(entry.getKey());
                cleaned++;
            }
        }

        if (cleaned > 0) {
            LOG.log(System.Logger.Level.INFO, "Cleaned up {0} expired sessions", cleaned);
        }
        return cleaned;
    }
}
