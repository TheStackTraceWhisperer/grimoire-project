package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ZoneTest {

    @Test
    void creation() {
        var zone = new Zone("forest_01");

        assertThat(zone.zoneId).isEqualTo("forest_01");
    }

    @Test
    void implementsComponent() {
        assertThat(new Zone("z")).isInstanceOf(Component.class);
    }

    @Test
    void noArgConstructor() {
        var zone = new Zone();
        assertThat(zone.zoneId).isNull();
    }

    @Test
    void update() {
        var zone = new Zone("old");
        zone.update("new");
        assertThat(zone.zoneId).isEqualTo("new");
    }

    @Test
    void equality() {
        assertThat(new Zone("a")).isEqualTo(new Zone("a"));
        assertThat(new Zone("a")).isNotEqualTo(new Zone("b"));
        assertThat(new Zone("a")).isNotEqualTo(null);
    }

    @Test
    void toStringFormat() {
        assertThat(new Zone("forest").toString()).contains("forest");
    }

    @Test
    void hashCodeConsistency() {
        assertThat(new Zone("a").hashCode()).isEqualTo(new Zone("a").hashCode());
    }

    @Test
    void hashCodeNull() {
        assertThat(new Zone().hashCode()).isZero();
    }
}
