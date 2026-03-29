package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MovementIntentTest {

    @Test
    void creation() {
        var mi = new MovementIntent(55.5, 77.3);

        assertThat(mi.targetX()).isEqualTo(55.5);
        assertThat(mi.targetY()).isEqualTo(77.3);
    }

    @Test
    void implementsComponent() {
        assertThat(new MovementIntent(0, 0)).isInstanceOf(Component.class);
    }
}
