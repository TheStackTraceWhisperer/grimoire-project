package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JoinGroupResponseTest {

    @Test
    void creationPreservesFields() {
        var resp = new JoinGroupResponse(true, "joined");

        assertThat(resp.success()).isTrue();
        assertThat(resp.message()).isEqualTo("joined");
    }
}
