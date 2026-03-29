package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoundingBoxTest {

    @Test
    void creation() {
        var bb = new BoundingBox(10.0, 20.0);

        assertThat(bb.width()).isEqualTo(10.0);
        assertThat(bb.height()).isEqualTo(20.0);
    }

    @Test
    void implementsComponent() {
        assertThat(new BoundingBox(8, 8)).isInstanceOf(Component.class);
    }

    @Test
    void zeroDimensions() {
        var bb = new BoundingBox(0, 0);

        assertThat(bb.width()).isZero();
        assertThat(bb.height()).isZero();
    }
}
