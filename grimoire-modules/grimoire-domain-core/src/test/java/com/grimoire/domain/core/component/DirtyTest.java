package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DirtyTest {

    @Test
    void creation() {
        var dirty = new Dirty(42L);

        assertThat(dirty.tick()).isEqualTo(42L);
    }

    @Test
    void implementsComponent() {
        assertThat(new Dirty(0)).isInstanceOf(Component.class);
    }
}
