package com.grimoire.shared.dto;

import com.grimoire.shared.component.ComponentDTO;
import com.grimoire.shared.component.PositionDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameStateUpdateTest {

    @Test
    void testGameStateUpdateCreation() {
        Map<String, List<ComponentDTO>> updates = new HashMap<>();
        updates.put("entity-1", List.of(new PositionDTO(10, 20)));
        
        GameStateUpdate update = new GameStateUpdate(123L, updates);
        
        assertEquals(123L, update.timestamp());
        assertEquals(1, update.entityUpdates().size());
        assertTrue(update.entityUpdates().containsKey("entity-1"));
    }

    @Test
    void testGameStateUpdateIsSerializable() {
        GameStateUpdate update = new GameStateUpdate(0L, Map.of());
        assertInstanceOf(java.io.Serializable.class, update);
    }

    @Test
    void testGameStateUpdateWithEmptyUpdates() {
        GameStateUpdate update = new GameStateUpdate(100L, Map.of());
        assertEquals(100L, update.timestamp());
        assertTrue(update.entityUpdates().isEmpty());
    }

    @Test
    void testGameStateUpdateDefensivelyCopiesMapAndLists() {
        List<ComponentDTO> components = new ArrayList<>();
        components.add(new PositionDTO(10, 20));

        Map<String, List<ComponentDTO>> source = new HashMap<>();
        source.put("entity-1", components);

        GameStateUpdate update = new GameStateUpdate(123L, source);

        source.clear();
        components.clear();

        assertEquals(1, update.entityUpdates().size());
        assertEquals(1, update.entityUpdates().get("entity-1").size());
        assertThrows(UnsupportedOperationException.class,
                () -> update.entityUpdates().put("entity-2", List.of()));
        assertThrows(UnsupportedOperationException.class,
                () -> update.entityUpdates().get("entity-1").add(new PositionDTO(30, 40)));
    }
}
