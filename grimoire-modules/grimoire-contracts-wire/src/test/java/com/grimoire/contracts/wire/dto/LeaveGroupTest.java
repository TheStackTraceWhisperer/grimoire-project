package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LeaveGroupTest {

    @Test
    void creationPreservesFields() {
        var req = new LeaveGroup("Raiders");
        assertThat(req.groupName()).isEqualTo("Raiders");
    }
}
