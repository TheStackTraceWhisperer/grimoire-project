package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DirtyTest {

    @Test
    void creation() {
        var dirty = new Dirty(42L);

        assertThat(dirty.tick).isEqualTo(42L);
    }

    @Test
    void implementsComponent() {
        assertThat(new Dirty(0)).isInstanceOf(Component.class);
    }

    @Test
    void noArgConstructor() {
        var dirty = new Dirty();
        assertThat(dirty.tick).isZero();
    }

    @Test
    void update() {
        var dirty = new Dirty(1);
        dirty.update(99L);
        assertThat(dirty.tick).isEqualTo(99L);
    }

    @Test
    void equality() {
        assertThat(new Dirty(42)).isEqualTo(new Dirty(42));
        assertThat(new Dirty(42)).isNotEqualTo(new Dirty(99));
        assertThat(new Dirty(42)).isNotEqualTo(null);
    }

    @Test
    void toStringFormat() {
        assertThat(new Dirty(42).toString()).contains("42");
    }

    @Test
    void hashCodeConsistency() {
        assertThat(new Dirty(42).hashCode()).isEqualTo(new Dirty(42).hashCode());
    }
}
