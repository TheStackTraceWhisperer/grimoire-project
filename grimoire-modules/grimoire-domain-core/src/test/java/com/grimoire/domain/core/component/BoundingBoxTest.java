package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoundingBoxTest {

    @Test
    void creation() {
        var bb = new BoundingBox(10.0, 20.0);

        assertThat(bb.width).isEqualTo(10.0);
        assertThat(bb.height).isEqualTo(20.0);
    }

    @Test
    void noArgConstructor() {
        var bb = new BoundingBox();

        assertThat(bb.width).isZero();
        assertThat(bb.height).isZero();
    }

    @Test
    void implementsComponent() {
        assertThat(new BoundingBox(8, 8)).isInstanceOf(Component.class);
    }

    @Test
    void zeroDimensions() {
        var bb = new BoundingBox(0, 0);

        assertThat(bb.width).isZero();
        assertThat(bb.height).isZero();
    }

    @Test
    void update() {
        var bb = new BoundingBox(1, 2);
        bb.update(10, 20);

        assertThat(bb.width).isEqualTo(10);
        assertThat(bb.height).isEqualTo(20);
    }

    @Test
    void equality() {
        assertThat(new BoundingBox(1, 2)).isEqualTo(new BoundingBox(1, 2));
        assertThat(new BoundingBox(1, 2)).isNotEqualTo(new BoundingBox(3, 4));
        assertThat(new BoundingBox(1, 2)).isNotEqualTo(null);
        assertThat(new BoundingBox(1, 2)).isNotEqualTo("other");
    }

    @Test
    void toStringFormat() {
        assertThat(new BoundingBox(5, 10).toString()).contains("5.0").contains("10.0");
    }

    @Test
    void hashCodeConsistency() {
        assertThat(new BoundingBox(1, 2).hashCode()).isEqualTo(new BoundingBox(1, 2).hashCode());
    }
}
