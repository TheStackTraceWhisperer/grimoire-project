package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LeaveGroupResponseTest {

    @Test
    void creationPreservesFields() {
        var resp = new LeaveGroupResponse(false, "not a member");

        assertThat(resp.success()).isFalse();
        assertThat(resp.message()).isEqualTo("not a member");
    }
}
