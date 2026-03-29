package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GroupMessageBroadcastTest {

    @Test
    void creationPreservesFields() {
        var broadcast = new GroupMessageBroadcast("Guild", "Player1", "hello guild");

        assertThat(broadcast.groupName()).isEqualTo("Guild");
        assertThat(broadcast.sender()).isEqualTo("Player1");
        assertThat(broadcast.message()).isEqualTo("hello guild");
    }
}
