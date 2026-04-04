package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MovementIntentTest {

    @Test
    void creation() {
        var mi = new MovementIntent(55.5, 77.3);

        assertThat(mi.targetX).isEqualTo(55.5);
        assertThat(mi.targetY).isEqualTo(77.3);
    }

    @Test
    void implementsComponent() {
        assertThat(new MovementIntent(0, 0)).isInstanceOf(Component.class);
    }

    @Test
    void noArgConstructor() {
        var mi = new MovementIntent();
        assertThat(mi.targetX).isZero();
        assertThat(mi.targetY).isZero();
    }

    @Test
    void update() {
        var mi = new MovementIntent(1, 2);
        mi.update(10, 20);
        assertThat(mi.targetX).isEqualTo(10);
        assertThat(mi.targetY).isEqualTo(20);
    }

    @Test
    void equality() {
        assertThat(new MovementIntent(1, 2)).isEqualTo(new MovementIntent(1, 2));
        assertThat(new MovementIntent(1, 2)).isNotEqualTo(new MovementIntent(3, 4));
        assertThat(new MovementIntent(1, 2)).isNotEqualTo(null);
    }

    @Test
    void toStringFormat() {
        assertThat(new MovementIntent(5, 10).toString()).contains("5.0").contains("10.0");
    }

    @Test
    void hashCodeConsistency() {
        assertThat(new MovementIntent(1, 2).hashCode()).isEqualTo(new MovementIntent(1, 2).hashCode());
    }
}
