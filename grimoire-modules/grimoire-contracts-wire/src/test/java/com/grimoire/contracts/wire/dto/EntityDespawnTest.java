package com.grimoire.contracts.wire.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntityDespawnTest {

    @Test
    void creationPreservesFields() {
        var despawn = new EntityDespawn("ent-99");
        assertThat(despawn.entityId()).isEqualTo("ent-99");
    }
}
