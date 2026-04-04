package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SolidTest {

    @Test
    void creation() {
        var solid = new Solid();

        assertThat(solid).isNotNull();
    }

    @Test
    void implementsComponent() {
        assertThat(new Solid()).isInstanceOf(Component.class);
    }

    @Test
    void equality() {
        assertThat(new Solid()).isEqualTo(new Solid());
    }

    @Test
    void toStringFormat() {
        assertThat(new Solid().toString()).contains("Solid");
    }
}
