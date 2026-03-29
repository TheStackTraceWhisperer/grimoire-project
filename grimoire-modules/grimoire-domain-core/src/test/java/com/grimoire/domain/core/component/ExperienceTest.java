package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExperienceTest {

    @Test
    void creation() {
        var xp = new Experience(150, 1000);

        assertThat(xp.currentXp()).isEqualTo(150);
        assertThat(xp.xpToNextLevel()).isEqualTo(1000);
    }

    @Test
    void implementsComponent() {
        assertThat(new Experience(0, 100)).isInstanceOf(Component.class);
    }
}
