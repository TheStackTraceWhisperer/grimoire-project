package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenLoginRequestTest {

    @Test
    void creationPreservesFields() {
        var req = new TokenLoginRequest("bearer-abc");
        assertThat(req.accessToken()).isEqualTo("bearer-abc");
    }

    @Test
    void implementsSerializable() {
        assertThat(new TokenLoginRequest("t")).isInstanceOf(java.io.Serializable.class);
    }
}
