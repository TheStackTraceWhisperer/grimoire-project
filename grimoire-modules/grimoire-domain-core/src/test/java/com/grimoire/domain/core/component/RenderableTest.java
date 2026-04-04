package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RenderableTest {

    @Test
    void creation() {
        var r = new Renderable("Goblin", "goblin_sprite_01");

        assertThat(r.name).isEqualTo("Goblin");
        assertThat(r.visualId).isEqualTo("goblin_sprite_01");
    }

    @Test
    void implementsComponent() {
        assertThat(new Renderable("a", "b")).isInstanceOf(Component.class);
    }

    @Test
    void noArgConstructor() {
        var r = new Renderable();
        assertThat(r.name).isNull();
        assertThat(r.visualId).isNull();
    }

    @Test
    void update() {
        var r = new Renderable("a", "b");
        r.update("c", "d");
        assertThat(r.name).isEqualTo("c");
        assertThat(r.visualId).isEqualTo("d");
    }

    @Test
    void equality() {
        assertThat(new Renderable("a", "b")).isEqualTo(new Renderable("a", "b"));
        assertThat(new Renderable("a", "b")).isNotEqualTo(new Renderable("x", "y"));
        assertThat(new Renderable("a", "b")).isNotEqualTo(null);
    }

    @Test
    void toStringFormat() {
        assertThat(new Renderable("Goblin", "sprite1").toString()).contains("Goblin").contains("sprite1");
    }

    @Test
    void hashCodeConsistency() {
        assertThat(new Renderable("a", "b").hashCode()).isEqualTo(new Renderable("a", "b").hashCode());
    }
}
