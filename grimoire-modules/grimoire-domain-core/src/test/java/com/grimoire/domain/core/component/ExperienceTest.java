package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExperienceTest {

    @Test
    void creation() {
        var xp = new Experience(150, 1000);

        assertThat(xp.currentXp).isEqualTo(150);
        assertThat(xp.xpToNextLevel).isEqualTo(1000);
    }

    @Test
    void implementsComponent() {
        assertThat(new Experience(0, 100)).isInstanceOf(Component.class);
    }

    @Test
    void noArgConstructor() {
        var xp = new Experience();
        assertThat(xp.currentXp).isZero();
        assertThat(xp.xpToNextLevel).isZero();
    }

    @Test
    void update() {
        var xp = new Experience(0, 100);
        xp.update(50, 200);
        assertThat(xp.currentXp).isEqualTo(50);
        assertThat(xp.xpToNextLevel).isEqualTo(200);
    }

    @Test
    void addXp() {
        var xp = new Experience(50, 100);
        xp.addXp(30);
        assertThat(xp.currentXp).isEqualTo(80);
    }

    @Test
    void equality() {
        assertThat(new Experience(10, 100)).isEqualTo(new Experience(10, 100));
        assertThat(new Experience(10, 100)).isNotEqualTo(new Experience(20, 100));
        assertThat(new Experience(10, 100)).isNotEqualTo(null);
    }

    @Test
    void toStringFormat() {
        assertThat(new Experience(50, 100).toString()).contains("50").contains("100");
    }

    @Test
    void hashCodeConsistency() {
        assertThat(new Experience(10, 100).hashCode()).isEqualTo(new Experience(10, 100).hashCode());
    }
}
