package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VelocityTest {

    @Test
    void creation() {
        var v = new Velocity(3.5, -1.2);

        assertThat(v.dx()).isEqualTo(3.5);
        assertThat(v.dy()).isEqualTo(-1.2);
    }

    @Test
    void implementsComponent() {
        assertThat(new Velocity(0, 0)).isInstanceOf(Component.class);
    }
}
