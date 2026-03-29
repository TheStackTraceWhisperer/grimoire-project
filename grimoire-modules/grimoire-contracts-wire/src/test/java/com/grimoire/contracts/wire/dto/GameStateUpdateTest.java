package com.grimoire.contracts.wire.dto;

import com.grimoire.contracts.wire.component.PositionDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GameStateUpdateTest {

    @Test
    void creationPreservesFields() {
        var update = new GameStateUpdate(
                123456L,
                Map.of("ent-1", List.of(new PositionDTO(5.0, 10.0))));

        assertThat(update.timestamp()).isEqualTo(123456L);
        assertThat(update.entityUpdates()).containsKey("ent-1");
    }

    @Test
    void nullEntityUpdatesDefaultsToEmpty() {
        var update = new GameStateUpdate(0L, null);
        assertThat(update.entityUpdates()).isEmpty();
    }
}
