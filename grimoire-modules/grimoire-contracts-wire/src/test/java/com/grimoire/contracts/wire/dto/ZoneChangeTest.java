package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ZoneChangeTest {

    @Test
    void creationPreservesFields() {
        var change = new ZoneChange("forest-2", 50.0, 75.0);

        assertThat(change.newZoneId()).isEqualTo("forest-2");
        assertThat(change.spawnX()).isEqualTo(50.0);
        assertThat(change.spawnY()).isEqualTo(75.0);
    }
}
