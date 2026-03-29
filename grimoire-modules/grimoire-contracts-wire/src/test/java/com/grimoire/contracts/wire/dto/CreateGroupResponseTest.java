package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateGroupResponseTest {

    @Test
    void creationPreservesFields() {
        var resp = new CreateGroupResponse(true, "created", 1L);

        assertThat(resp.success()).isTrue();
        assertThat(resp.message()).isEqualTo("created");
        assertThat(resp.groupId()).isEqualTo(1L);
    }
}
