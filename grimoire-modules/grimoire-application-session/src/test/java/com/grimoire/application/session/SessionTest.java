package com.grimoire.application.session;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionTest {

    @Test
    void createProducesNonNullFields() {
        Session session = Session.create("acc-1", "player1", 30);

        assertThat(session.sessionId()).isNotNull().isNotBlank();
        assertThat(session.accountId()).isEqualTo("acc-1");
        assertThat(session.username()).isEqualTo("player1");
        assertThat(session.createdAt()).isNotNull();
        assertThat(session.expiresAt()).isNotNull();
    }

    @Test
    void freshSessionIsNotExpired() {
        Session session = Session.create("acc-1", "player1", 30);

        assertThat(session.isExpired()).isFalse();
    }

    @Test
    void sessionWithPastExpiryIsExpired() {
        Session session = new Session(
                "sid",
                "acc-1",
                "player1",
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(60));

        assertThat(session.isExpired()).isTrue();
    }

    @Test
    void zeroMinuteValidityCreatesImmediatelyExpiredSession() {
        Session session = Session.create("acc-1", "player1", 0);

        assertThat(session.isExpired()).isTrue();
    }

    @Test
    void expiresAtIsInFutureForPositiveValidity() {
        Session session = Session.create("acc-1", "player1", 60);

        assertThat(session.expiresAt()).isAfter(Instant.now());
    }

    @Test
    void uniqueSessionIdsFromFactory() {
        Session s1 = Session.create("acc-1", "player1", 30);
        Session s2 = Session.create("acc-1", "player1", 30);

        assertThat(s1.sessionId()).isNotEqualTo(s2.sessionId());
    }

    @Test
    void recordEquality() {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(1800);
        Session s1 = new Session("sid", "acc", "user", now, exp);
        Session s2 = new Session("sid", "acc", "user", now, exp);

        assertThat(s1).isEqualTo(s2);
    }

    @Test
    void nullSessionIdRejected() {
        assertThatThrownBy(() -> new Session(null, "acc", "user", Instant.now(), Instant.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullAccountIdRejected() {
        assertThatThrownBy(() -> new Session("sid", null, "user", Instant.now(), Instant.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullUsernameRejected() {
        assertThatThrownBy(() -> new Session("sid", "acc", null, Instant.now(), Instant.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullCreatedAtRejected() {
        assertThatThrownBy(() -> new Session("sid", "acc", "user", null, Instant.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullExpiresAtRejected() {
        assertThatThrownBy(() -> new Session("sid", "acc", "user", Instant.now(), null))
                .isInstanceOf(NullPointerException.class);
    }
}
