package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginFailureTest {

    @Test
    void creationPreservesFields() {
        var failure = new LoginFailure("invalid token");
        assertThat(failure.reason()).isEqualTo("invalid token");
    }
}
