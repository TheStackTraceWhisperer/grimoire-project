package com.grimoire.contracts.wire.dto;

import com.grimoire.contracts.wire.component.PositionDTO;
import com.grimoire.contracts.wire.component.StatsDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EntitySpawnTest {

    @Test
    void creationPreservesFields() {
        var spawn = new EntitySpawn("ent-42", List.of(new PositionDTO(1, 2), new StatsDTO(100, 100)));

        assertThat(spawn.entityId()).isEqualTo("ent-42");
        assertThat(spawn.allComponents()).hasSize(2);
    }

    @Test
    void nullComponentsDefaultsToEmpty() {
        var spawn = new EntitySpawn("ent-1", null);
        assertThat(spawn.allComponents()).isEmpty();
    }
}
