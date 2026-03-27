package com.grimoire.shared.dto;

import com.grimoire.shared.component.ComponentDTO;
import com.grimoire.shared.component.PositionDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntitySpawnTest {

    @Test
    void testEntitySpawnCreation() {
        List<ComponentDTO> components = List.of(new PositionDTO(100, 200));
        EntitySpawn spawn = new EntitySpawn("entity-123", components);
        
        assertEquals("entity-123", spawn.entityId());
        assertEquals(1, spawn.allComponents().size());
        assertInstanceOf(PositionDTO.class, spawn.allComponents().get(0));
    }

    @Test
    void testEntitySpawnIsSerializable() {
        EntitySpawn spawn = new EntitySpawn("entity-456", List.of());
        assertInstanceOf(java.io.Serializable.class, spawn);
    }

    @Test
    void testEntitySpawnWithEmptyComponents() {
        EntitySpawn spawn = new EntitySpawn("entity-789", List.of());
        assertEquals("entity-789", spawn.entityId());
        assertTrue(spawn.allComponents().isEmpty());
    }
}
