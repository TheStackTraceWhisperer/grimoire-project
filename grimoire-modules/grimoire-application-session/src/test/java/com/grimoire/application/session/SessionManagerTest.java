package com.grimoire.application.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionManagerTest {

    /**
     * Simple test config stub.
     */
    private static final SessionConfig TEST_CONFIG = () -> 30; // 30 minutes
    private SessionManager manager;

    @BeforeEach
    void setUp() {
        manager = new SessionManager(TEST_CONFIG);
    }

    // ── Creation ──

    @Test
    void createSessionReturnsNonNullSession() {
        Session session = manager.createSession("acc-1", "player1");

        assertThat(session).isNotNull();
        assertThat(session.sessionId()).isNotNull();
        assertThat(session.accountId()).isEqualTo("acc-1");
        assertThat(session.username()).isEqualTo("player1");
        assertThat(session.isExpired()).isFalse();
    }

    @Test
    void createSessionIncrementsCount() {
        manager.createSession("acc-1", "player1");
        manager.createSession("acc-2", "player2");

        assertThat(manager.getActiveSessionCount()).isEqualTo(2);
    }

    @Test
    void createSessionInvalidatesPreviousForSameAccount() {
        Session first = manager.createSession("acc-1", "player1");
        Session second = manager.createSession("acc-1", "player1");

        assertThat(manager.validateSession(first.sessionId())).isEmpty();
        assertThat(manager.validateSession(second.sessionId())).isPresent();
        assertThat(manager.getActiveSessionCount()).isEqualTo(1);
    }

    @Test
    void createSessionRejectsNullAccountId() {
        assertThatThrownBy(() -> manager.createSession(null, "user"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createSessionRejectsNullUsername() {
        assertThatThrownBy(() -> manager.createSession("acc-1", null))
                .isInstanceOf(NullPointerException.class);
    }

    // ── Validation ──

    @Test
    void validateSessionReturnsSessionWhenValid() {
        Session session = manager.createSession("acc-1", "player1");

        Optional<Session> validated = manager.validateSession(session.sessionId());

        assertThat(validated).isPresent();
        assertThat(validated.get().sessionId()).isEqualTo(session.sessionId());
    }

    @Test
    void validateSessionReturnsEmptyForUnknownId() {
        assertThat(manager.validateSession("does-not-exist")).isEmpty();
    }

    @Test
    void validateSessionAutoInvalidatesExpired() {
        // Inject an expired session via createSession with 0-minute config
        SessionManager shortManager = new SessionManager(() -> 0);
        Session expired = shortManager.createSession("acc-1", "player1");

        Optional<Session> validated = shortManager.validateSession(expired.sessionId());

        assertThat(validated).isEmpty();
        assertThat(shortManager.getActiveSessionCount()).isZero();
    }

    // ── Invalidation ──

    @Test
    void invalidateSessionRemovesIt() {
        Session session = manager.createSession("acc-1", "player1");

        manager.invalidateSession(session.sessionId());

        assertThat(manager.validateSession(session.sessionId())).isEmpty();
        assertThat(manager.getActiveSessionCount()).isZero();
    }

    @Test
    void invalidateSessionDoesNotThrowForUnknownId() {
        manager.invalidateSession("non-existent");

        assertThat(manager.getActiveSessionCount()).isZero();
    }

    @Test
    void invalidateSessionTwiceDoesNotThrow() {
        Session session = manager.createSession("acc-1", "player1");

        manager.invalidateSession(session.sessionId());
        manager.invalidateSession(session.sessionId());

        assertThat(manager.getActiveSessionCount()).isZero();
    }

    @Test
    void invalidateSessionForAccountRemovesIt() {
        Session session = manager.createSession("acc-1", "player1");

        manager.invalidateSessionForAccount("acc-1");

        assertThat(manager.validateSession(session.sessionId())).isEmpty();
        assertThat(manager.getActiveSessionCount()).isZero();
    }

    @Test
    void invalidateSessionForNonExistentAccountDoesNotThrow() {
        manager.invalidateSessionForAccount("unknown-account");

        assertThat(manager.getActiveSessionCount()).isZero();
    }

    // ── Lookup by account ──

    @Test
    void getSessionForAccountReturnsSessionWhenValid() {
        manager.createSession("acc-1", "player1");

        Optional<Session> result = manager.getSessionForAccount("acc-1");

        assertThat(result).isPresent();
        assertThat(result.get().accountId()).isEqualTo("acc-1");
    }

    @Test
    void getSessionForAccountReturnsEmptyWhenNoSession() {
        assertThat(manager.getSessionForAccount("unknown")).isEmpty();
    }

    @Test
    void getSessionForAccountReturnsEmptyAfterInvalidation() {
        manager.createSession("acc-1", "player1");
        manager.invalidateSessionForAccount("acc-1");

        assertThat(manager.getSessionForAccount("acc-1")).isEmpty();
    }

    // ── Cleanup ──

    @Test
    void cleanupExpiredSessionsRemovesExpired() {
        SessionManager shortManager = new SessionManager(() -> 0);
        shortManager.createSession("acc-1", "player1");
        shortManager.createSession("acc-2", "player2");

        int cleaned = shortManager.cleanupExpiredSessions();

        assertThat(cleaned).isEqualTo(2);
        assertThat(shortManager.getActiveSessionCount()).isZero();
    }

    @Test
    void cleanupExpiredSessionsLeavesActiveSessions() {
        manager.createSession("acc-1", "player1"); // 30-minute validity

        int cleaned = manager.cleanupExpiredSessions();

        assertThat(cleaned).isZero();
        assertThat(manager.getActiveSessionCount()).isEqualTo(1);
    }

    @Test
    void cleanupExpiredSessionsReturnsZeroWhenEmpty() {
        assertThat(manager.cleanupExpiredSessions()).isZero();
    }

    // ── Multiple accounts ──

    @Test
    void multipleAccountSessionsAreIndependent() {
        Session s1 = manager.createSession("acc-1", "player1");
        Session s2 = manager.createSession("acc-2", "player2");

        manager.invalidateSession(s1.sessionId());

        assertThat(manager.validateSession(s1.sessionId())).isEmpty();
        assertThat(manager.validateSession(s2.sessionId())).isPresent();
        assertThat(manager.getActiveSessionCount()).isEqualTo(1);
    }

    @Test
    void sessionIdsAreUnique() {
        Session s1 = manager.createSession("acc-1", "player1");
        Session s2 = manager.createSession("acc-2", "player2");

        assertThat(s1.sessionId()).isNotEqualTo(s2.sessionId());
    }

    // ── Constructor ──

    @Test
    void constructorRejectsNullConfig() {
        assertThatThrownBy(() -> new SessionManager(null))
                .isInstanceOf(NullPointerException.class);
    }
}
