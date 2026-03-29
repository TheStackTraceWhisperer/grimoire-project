package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ZoneTest {

    @Test
    void creation() {
        var zone = new Zone("forest_01");

        assertThat(zone.zoneId()).isEqualTo("forest_01");
    }

    @Test
    void implementsComponent() {
        assertThat(new Zone("z")).isInstanceOf(Component.class);
    }
}
