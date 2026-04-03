package com.grimoire.testkit.fake;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FakeSessionConfig}.
 */
class FakeSessionConfigTest {

    @Test
    void defaultConstructorReturnsThirtyMinutes() {
        FakeSessionConfig config = new FakeSessionConfig();

        assertThat(config.sessionValidityMinutes()).isEqualTo(30);
    }

    @Test
    void customValueIsPreserved() {
        FakeSessionConfig config = new FakeSessionConfig(60);

        assertThat(config.sessionValidityMinutes()).isEqualTo(60);
    }

    @Test
    void zeroMinutesIsAllowed() {
        FakeSessionConfig config = new FakeSessionConfig(0);

        assertThat(config.sessionValidityMinutes()).isZero();
    }
}
