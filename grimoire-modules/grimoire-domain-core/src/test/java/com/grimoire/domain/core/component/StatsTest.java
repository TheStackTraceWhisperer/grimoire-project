package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatsTest {

    @Test
    void creation() {
        var stats = new Stats(50, 100, 5, 10);

        assertThat(stats.hp).isEqualTo(50);
        assertThat(stats.maxHp).isEqualTo(100);
        assertThat(stats.defense).isEqualTo(5);
        assertThat(stats.attack).isEqualTo(10);
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

    @Test
    void noArgConstructor() {
        var stats = new Stats();
        assertThat(stats.hp).isZero();
        assertThat(stats.maxHp).isZero();
        assertThat(stats.defense).isZero();
        assertThat(stats.attack).isZero();
    }

    @Test
    void update() {
        var stats = new Stats(1, 2, 3, 4);
        stats.update(10, 20, 30, 40);
        assertThat(stats.hp).isEqualTo(10);
        assertThat(stats.maxHp).isEqualTo(20);
        assertThat(stats.defense).isEqualTo(30);
        assertThat(stats.attack).isEqualTo(40);
    }

    @Test
    void applyDamage() {
        var stats = new Stats(100, 100, 5, 10);
        stats.applyDamage(30);
        assertThat(stats.hp).isEqualTo(70);
    }

    @Test
    void applyDamageFloorsAtZero() {
        var stats = new Stats(10, 100, 5, 10);
        stats.applyDamage(999);
        assertThat(stats.hp).isZero();
    }

    @Test
    void toStringFormat() {
        assertThat(new Stats(50, 100, 5, 10).toString()).contains("50").contains("100");
    }

    @Test
    void hashCodeConsistency() {
        assertThat(new Stats(10, 20, 3, 5).hashCode()).isEqualTo(new Stats(10, 20, 3, 5).hashCode());
    }
}
