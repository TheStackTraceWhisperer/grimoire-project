package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeadTest {

    @Test
    void creationWithKiller() {
        var dead = new Dead(42);

        assertThat(dead.killerId).isEqualTo(42);
    }

    @Test
    void creationEnvironmentalDeath() {
        var dead = new Dead(-1);

        assertThat(dead.killerId).isEqualTo(-1);
    }

    @Test
    void implementsComponent() {
        assertThat(new Dead(-1)).isInstanceOf(Component.class);
    }

    @Test
    void noArgConstructorDefaultsToEnvironmental() {
        var dead = new Dead();
        assertThat(dead.killerId).isEqualTo(-1);
    }

    @Test
    void update() {
        var dead = new Dead(-1);
        dead.update(99);
        assertThat(dead.killerId).isEqualTo(99);
    }

    @Test
    void equality() {
        assertThat(new Dead(42)).isEqualTo(new Dead(42));
        assertThat(new Dead(42)).isNotEqualTo(new Dead(99));
        assertThat(new Dead(42)).isNotEqualTo(null);
        assertThat(new Dead(42)).isNotEqualTo("other");
    }

    @Test
    void toStringFormat() {
        assertThat(new Dead(42).toString()).contains("42");
    }

    @Test
    void hashCodeConsistency() {
        assertThat(new Dead(42).hashCode()).isEqualTo(new Dead(42).hashCode());
    }
}
