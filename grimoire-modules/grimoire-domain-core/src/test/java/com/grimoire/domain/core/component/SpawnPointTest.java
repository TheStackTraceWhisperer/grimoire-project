package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpawnPointTest {

    @Test
    void creation() {
        var sp = new SpawnPoint(100.0, 200.0, 150.0);

        assertThat(sp.x()).isEqualTo(100.0);
        assertThat(sp.y()).isEqualTo(200.0);
        assertThat(sp.leashRadius()).isEqualTo(150.0);
    }

    @Test
    void implementsComponent() {
        assertThat(new SpawnPoint(0, 0, 0)).isInstanceOf(Component.class);
    }

    @Test
    void equality() {
        assertThat(new SpawnPoint(1, 2, 3)).isEqualTo(new SpawnPoint(1, 2, 3));
        assertThat(new SpawnPoint(1, 2, 3)).isNotEqualTo(new SpawnPoint(4, 5, 6));
    }
}
