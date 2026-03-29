package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrivateMessageTest {

    @Test
    void creationPreservesFields() {
        var pm = new PrivateMessage("Player2", "secret");

        assertThat(pm.recipientName()).isEqualTo("Player2");
        assertThat(pm.message()).isEqualTo("secret");
    }
}
