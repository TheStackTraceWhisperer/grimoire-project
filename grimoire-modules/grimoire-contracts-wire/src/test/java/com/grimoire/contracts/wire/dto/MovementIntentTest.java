package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MovementIntentTest {

    @Test
    void creationPreservesFields() {
        var intent = new MovementIntent(100.5, 200.3);

        assertThat(intent.targetX()).isEqualTo(100.5);
        assertThat(intent.targetY()).isEqualTo(200.3);
    }
}
