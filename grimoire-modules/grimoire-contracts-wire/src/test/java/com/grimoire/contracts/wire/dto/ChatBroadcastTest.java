package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatBroadcastTest {

    @Test
    void creationPreservesFields() {
        var broadcast = new ChatBroadcast("Player1", "hello");

        assertThat(broadcast.sender()).isEqualTo("Player1");
        assertThat(broadcast.message()).isEqualTo("hello");
    }
}
