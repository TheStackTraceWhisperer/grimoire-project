package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PortalTest {

    @Test
    void creation() {
        var portal = new Portal("dungeon_02", "exit_north");

        assertThat(portal.targetZoneId).isEqualTo("dungeon_02");
        assertThat(portal.targetPortalId).isEqualTo("exit_north");
    }

    @Test
    void implementsComponent() {
        assertThat(new Portal("a", "b")).isInstanceOf(Component.class);
    }

    @Test
    void noArgConstructor() {
        var p = new Portal();
        assertThat(p.targetZoneId).isNull();
        assertThat(p.targetPortalId).isNull();
    }

    @Test
    void update() {
        var p = new Portal("a", "b");
        p.update("c", "d");
        assertThat(p.targetZoneId).isEqualTo("c");
        assertThat(p.targetPortalId).isEqualTo("d");
    }

    @Test
    void equality() {
        assertThat(new Portal("a", "b")).isEqualTo(new Portal("a", "b"));
        assertThat(new Portal("a", "b")).isNotEqualTo(new Portal("x", "y"));
        assertThat(new Portal("a", "b")).isNotEqualTo(null);
    }

    @Test
    void toStringFormat() {
        assertThat(new Portal("zone1", "exit").toString()).contains("zone1").contains("exit");
    }

    @Test
    void hashCodeConsistency() {
        assertThat(new Portal("a", "b").hashCode()).isEqualTo(new Portal("a", "b").hashCode());
    }

    @Test
    void hashCodeNullFields() {
        assertThat(new Portal().hashCode()).isEqualTo(0);
    }
}
