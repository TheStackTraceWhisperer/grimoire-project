package com.grimoire.domain.core.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PortalTest {

    @Test
    void creation() {
        var portal = new Portal("dungeon_02", "exit_north");

        assertThat(portal.targetZoneId()).isEqualTo("dungeon_02");
        assertThat(portal.targetPortalId()).isEqualTo("exit_north");
    }

    @Test
    void implementsComponent() {
        assertThat(new Portal("a", "b")).isInstanceOf(Component.class);
    }
}
