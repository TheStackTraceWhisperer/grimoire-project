package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GroupMessageTest {

    @Test
    void creationPreservesFields() {
        var msg = new GroupMessage("Guild", "attack!");

        assertThat(msg.groupName()).isEqualTo("Guild");
        assertThat(msg.message()).isEqualTo("attack!");
    }
}
