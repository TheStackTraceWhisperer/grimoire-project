package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RenderableTest {

    @Test
    void creation() {
        var r = new Renderable("Goblin", "goblin_sprite_01");

        assertThat(r.name()).isEqualTo("Goblin");
        assertThat(r.visualId()).isEqualTo("goblin_sprite_01");
    }

    @Test
    void implementsComponent() {
        assertThat(new Renderable("a", "b")).isInstanceOf(Component.class);
    }
}
