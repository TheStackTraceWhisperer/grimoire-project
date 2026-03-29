package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateGroupTest {

    @Test
    void creationPreservesFields() {
        var req = new CreateGroup("Raiders");
        assertThat(req.groupName()).isEqualTo("Raiders");
    }
}
