package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatsTest {

    @Test
    void creation() {
        var stats = new Stats(50, 100, 5, 10);

        assertThat(stats.hp()).isEqualTo(50);
        assertThat(stats.maxHp()).isEqualTo(100);
        assertThat(stats.defense()).isEqualTo(5);
        assertThat(stats.attack()).isEqualTo(10);
    }

    @Test
    void implementsComponent() {
        assertThat(new Stats(1, 1, 0, 0)).isInstanceOf(Component.class);
    }

    @Test
    void equality() {
        assertThat(new Stats(10, 20, 3, 5)).isEqualTo(new Stats(10, 20, 3, 5));
        assertThat(new Stats(10, 20, 3, 5)).isNotEqualTo(new Stats(10, 20, 3, 6));
    }
}
