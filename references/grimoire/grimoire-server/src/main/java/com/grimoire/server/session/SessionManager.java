package com.grimoire.server.session;

import com.grimoire.server.config.GameConfig;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages player sessions with automatic expiration.
 */
@Singleton
@Slf4j
public class SessionManager {
    
    private static final int CLEANUP_INTERVAL_SECONDS = 300; // 5 minutes
    
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Map<Long, String> accountToSession = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    private final int sessionValidityMinutes;
    
    public SessionManager(GameConfig gameConfig) {
        this.sessionValidityMinutes = gameConfig.getSessionValidityMinutes();
        // Start periodic cleanup of expired sessions
        cleanupExecutor.scheduleAtFixedRate(
                this::cleanupExpiredSessions,
                CLEANUP_INTERVAL_SECONDS,
                CLEANUP_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }
    
    /**
     * Creates a new session for an account.
     */
    public Session createSession(Long accountId, String username) {
        // Invalidate any existing session for this account
        invalidateSessionForAccount(accountId);
        
        Session session = Session.create(accountId, username, sessionValidityMinutes);
        sessions.put(session.sessionId(), session);
        accountToSession.put(accountId, session.sessionId());
        
        log.info("Created session {} for account {}", session.sessionId(), username);
        return session;
    }
    
    /**
     * Validates a session and returns it if valid.
     */
    public Optional<Session> validateSession(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null && !session.isExpired()) {
            return Optional.of(session);
        }
        
        if (session != null && session.isExpired()) {
            invalidateSession(sessionId);
        }
        
        return Optional.empty();
    }
    
    /**
     * Invalidates a session by session ID.
     */
    public void invalidateSession(String sessionId) {
        Session session = sessions.remove(sessionId);
        if (session != null) {
            accountToSession.remove(session.accountId());
            log.info("Invalidated session {} for account {}", sessionId, session.username());
        }
    }
    
    /**
     * Invalidates all sessions for an account.
     */
    public void invalidateSessionForAccount(Long accountId) {
        String sessionId = accountToSession.remove(accountId);
        if (sessionId != null) {
            sessions.remove(sessionId);
            log.info("Invalidated session for account ID {}", accountId);
        }
    }
    
    /**
     * Cleans up expired sessions.
     */
    private void cleanupExpiredSessions() {
        Instant now = Instant.now();
        int cleaned = 0;
        
        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            if (now.isAfter(entry.getValue().expiresAt())) {
                invalidateSession(entry.getKey());
                cleaned++;
            }
        }
        
        if (cleaned > 0) {
            log.info("Cleaned up {} expired sessions", cleaned);
        }
    }
    
    /**
     * Gets the session for an account if it exists and is valid.
     */
    public Optional<Session> getSessionForAccount(Long accountId) {
        String sessionId = accountToSession.get(accountId);
        if (sessionId != null) {
            return validateSession(sessionId);
        }
        return Optional.empty();
    }
    
    /**
     * Gets the total number of active sessions.
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }
    
    /**
     * Shuts down the cleanup executor.
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}
