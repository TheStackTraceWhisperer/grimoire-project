package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrivateMessageBroadcastTest {

    @Test
    void creationPreservesFields() {
        var broadcast = new PrivateMessageBroadcast("Sender", "whisper");

        assertThat(broadcast.sender()).isEqualTo("Sender");
        assertThat(broadcast.message()).isEqualTo("whisper");
    }
}
