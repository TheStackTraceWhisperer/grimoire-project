package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpawnPointTest {

    @Test
    void creation() {
        var sp = new SpawnPoint(100.0, 200.0, 150.0);

        assertThat(sp.x).isEqualTo(100.0);
        assertThat(sp.y).isEqualTo(200.0);
        assertThat(sp.leashRadius).isEqualTo(150.0);
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

    @Test
    void noArgConstructor() {
        var sp = new SpawnPoint();
        assertThat(sp.x).isZero();
        assertThat(sp.y).isZero();
        assertThat(sp.leashRadius).isZero();
    }

    @Test
    void update() {
        var sp = new SpawnPoint(1, 2, 3);
        sp.update(10, 20, 30);
        assertThat(sp.x).isEqualTo(10);
        assertThat(sp.y).isEqualTo(20);
        assertThat(sp.leashRadius).isEqualTo(30);
    }

    @Test
    void toStringFormat() {
        assertThat(new SpawnPoint(1, 2, 3).toString()).contains("1.0").contains("2.0").contains("3.0");
    }

    @Test
    void hashCodeConsistency() {
        assertThat(new SpawnPoint(1, 2, 3).hashCode()).isEqualTo(new SpawnPoint(1, 2, 3).hashCode());
    }
}
