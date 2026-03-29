package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMessageTest {

    @Test
    void creationPreservesFields() {
        var msg = new ChatMessage("hello world");
        assertThat(msg.message()).isEqualTo("hello world");
    }
}
