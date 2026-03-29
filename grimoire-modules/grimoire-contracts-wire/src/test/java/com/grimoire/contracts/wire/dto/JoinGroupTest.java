package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JoinGroupTest {

    @Test
    void creationPreservesFields() {
        var req = new JoinGroup("Raiders");
        assertThat(req.groupName()).isEqualTo("Raiders");
    }
}
