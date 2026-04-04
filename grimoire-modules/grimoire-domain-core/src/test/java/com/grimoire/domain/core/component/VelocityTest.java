package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VelocityTest {

    @Test
    void creation() {
        var v = new Velocity(3.5, -1.2);

        assertThat(v.dx).isEqualTo(3.5);
        assertThat(v.dy).isEqualTo(-1.2);
    }

    @Test
    void noArgConstructor() {
        var v = new Velocity();

        assertThat(v.dx).isZero();
        assertThat(v.dy).isZero();
    }

    @Test
    void implementsComponent() {
        assertThat(new Velocity(0, 0)).isInstanceOf(Component.class);
    }

    @Test
    void update() {
        var v = new Velocity(1, 2);
        v.update(10, 20);

        assertThat(v.dx).isEqualTo(10);
        assertThat(v.dy).isEqualTo(20);
    }

    @Test
    void equality() {
        assertThat(new Velocity(1, 2)).isEqualTo(new Velocity(1, 2));
        assertThat(new Velocity(1, 2)).isNotEqualTo(new Velocity(3, 4));
        assertThat(new Velocity(1, 2)).isNotEqualTo(null);
        assertThat(new Velocity(1, 2)).isNotEqualTo("other");
    }

    @Test
    void toStringFormat() {
        assertThat(new Velocity(1, 2).toString()).contains("1.0").contains("2.0");
    }

    @Test
    void hashCodeConsistency() {
        assertThat(new Velocity(1, 2).hashCode()).isEqualTo(new Velocity(1, 2).hashCode());
    }
}
