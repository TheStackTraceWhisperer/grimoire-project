package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PositionTest {

    @Test
    void creation() {
        var pos = new Position(100.5, 200.3);

        assertThat(pos.x).isEqualTo(100.5);
        assertThat(pos.y).isEqualTo(200.3);
    }

    @Test
    void noArgConstructor() {
        var pos = new Position();
        assertThat(pos.x).isZero();
        assertThat(pos.y).isZero();
    }

    @Test
    void implementsComponent() {
        assertThat(new Position(0, 0)).isInstanceOf(Component.class);
    }

    @Test
    void equality() {
        assertThat(new Position(1, 2)).isEqualTo(new Position(1, 2));
        assertThat(new Position(1, 2)).isNotEqualTo(new Position(3, 4));
    }

    @Test
    void equalsSameInstance() {
        var pos = new Position(1, 2);
        assertThat(pos).isEqualTo(pos);
    }

    @Test
    void equalsNull() {
        assertThat(new Position(1, 2)).isNotEqualTo(null);
    }

    @Test
    void equalsDifferentType() {
        assertThat(new Position(1, 2)).isNotEqualTo("not a position");
    }

    @Test
    void update() {
        var pos = new Position(1, 2);
        pos.update(10, 20);
        assertThat(pos.x).isEqualTo(10);
        assertThat(pos.y).isEqualTo(20);
    }

    @Test
    void translate() {
        var pos = new Position(10, 20);
        pos.translate(5, -3);
        assertThat(pos.x).isEqualTo(15);
        assertThat(pos.y).isEqualTo(17);
    }

    @Test
    void toStringFormat() {
        assertThat(new Position(1.5, 2.5).toString()).contains("1.5").contains("2.5");
    }

    @Test
    void hashCodeConsistency() {
        assertThat(new Position(1, 2).hashCode()).isEqualTo(new Position(1, 2).hashCode());
    }
}
