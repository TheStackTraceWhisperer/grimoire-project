package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeadTest {

    @Test
    void creationWithKiller() {
        var dead = new Dead("player-42");

        assertThat(dead.killerId()).isEqualTo("player-42");
    }

    @Test
    void creationEnvironmentalDeath() {
        var dead = new Dead(null);

        assertThat(dead.killerId()).isNull();
    }

    @Test
    void implementsComponent() {
        assertThat(new Dead(null)).isInstanceOf(Component.class);
    }
}
