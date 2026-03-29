package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PositionTest {

    @Test
    void creation() {
        var pos = new Position(100.5, 200.3);

        assertThat(pos.x()).isEqualTo(100.5);
        assertThat(pos.y()).isEqualTo(200.3);
    }

    @Test
    void implementsComponent() {
        assertThat(new Position(0, 0)).isInstanceOf(Component.class);
    }

    @Test
    void equality() {
        assertThat(new Position(1, 2)).isEqualTo(new Position(1, 2));
        assertThat(new Position(1, 2)).isNotEqualTo(new Position(3, 4));
    }
}
