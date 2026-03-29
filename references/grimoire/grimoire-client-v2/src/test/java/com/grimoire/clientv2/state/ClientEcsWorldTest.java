package com.grimoire.clientv2.state;

import com.grimoire.shared.component.PositionDTO;
import com.grimoire.shared.component.RenderableDTO;
import com.grimoire.shared.dto.EntityDespawn;
import com.grimoire.shared.dto.EntitySpawn;
import com.grimoire.shared.dto.GameStateUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ClientEcsWorld class.
 */
class ClientEcsWorldTest {
    
    private ClientEcsWorld ecsWorld;
    
    @BeforeEach
    void setUp() {
        ecsWorld = new ClientEcsWorld();
    }
    
    @Test
    void testSpawnEntity() {
        // Given
        EntitySpawn spawn = new EntitySpawn(
            "entity-1",
            List.of(
                new PositionDTO(100.0, 200.0),
                new RenderableDTO("TestPlayer", "visual-player")
            )
        );
        
        // When
        ecsWorld.spawnEntity(spawn);
        
        // Then
        assertEquals(1, ecsWorld.getEntityCount());
        assertNotNull(ecsWorld.getEntity("entity-1"));
        
        PositionDTO pos = ecsWorld.getComponent("entity-1", PositionDTO.class);
        assertNotNull(pos);
        assertEquals(100.0, pos.x());
        assertEquals(200.0, pos.y());
        
        RenderableDTO render = ecsWorld.getComponent("entity-1", RenderableDTO.class);
        assertNotNull(render);
        assertEquals("visual-player", render.visualId());
        assertEquals("TestPlayer", render.name());
    }
    
    @Test
    void testDespawnEntity() {
        // Given
        EntitySpawn spawn = new EntitySpawn(
            "entity-1",
            List.of(new PositionDTO(100.0, 200.0))
        );
        ecsWorld.spawnEntity(spawn);
        assertEquals(1, ecsWorld.getEntityCount());
        
        // When
        ecsWorld.despawnEntity(new EntityDespawn("entity-1"));
        
        // Then
        assertEquals(0, ecsWorld.getEntityCount());
        assertNull(ecsWorld.getEntity("entity-1"));
    }
    
    @Test
    void testProcessStateUpdate() {
        // Given - spawn an entity first
        EntitySpawn spawn = new EntitySpawn(
            "entity-1",
            List.of(new PositionDTO(100.0, 200.0))
        );
        ecsWorld.spawnEntity(spawn);
        
        // When - update the position
        GameStateUpdate update = new GameStateUpdate(
            System.currentTimeMillis(),
            Map.of("entity-1", List.of(new PositionDTO(150.0, 250.0)))
        );
        ecsWorld.processStateUpdate(update);
        
        // Then
        PositionDTO pos = ecsWorld.getComponent("entity-1", PositionDTO.class);
        assertNotNull(pos);
        assertEquals(150.0, pos.x());
        assertEquals(250.0, pos.y());
    }
    
    @Test
    void testClearAllEntities() {
        // Given
        ecsWorld.spawnEntity(new EntitySpawn("entity-1", List.of(new PositionDTO(0, 0))));
        ecsWorld.spawnEntity(new EntitySpawn("entity-2", List.of(new PositionDTO(0, 0))));
        assertEquals(2, ecsWorld.getEntityCount());
        
        // When
        ecsWorld.clearAllEntities();
        
        // Then
        assertEquals(0, ecsWorld.getEntityCount());
    }
    
    @Test
    void testLocalPlayerAndZone() {
        // When
        ecsWorld.setLocalPlayerEntityId("player-123");
        ecsWorld.setCurrentZone("dungeon-1");
        
        // Then
        assertEquals("player-123", ecsWorld.getLocalPlayerEntityId());
        assertEquals("dungeon-1", ecsWorld.getCurrentZone());
    }
    
    @Test
    void testEntitySpawnListener() {
        // Given
        AtomicBoolean listenerCalled = new AtomicBoolean(false);
        ecsWorld.setEntitySpawnListener((entityId, spawn) -> {
            assertEquals("entity-1", entityId);
            listenerCalled.set(true);
        });
        
        // When
        ecsWorld.spawnEntity(new EntitySpawn("entity-1", List.of(new PositionDTO(0, 0))));
        
        // Then
        assertTrue(listenerCalled.get());
    }
    
    @Test
    void testCreateEntityFromPrefab() {
        // Given
        ClientPrefab prefab = ClientPrefab.builder("player")
            .with(new PositionDTO(0, 0))
            .with(new RenderableDTO("Player", "visual-player"))
            .build();
        ecsWorld.registerPrefab(prefab);
        
        // When
        ecsWorld.createEntityFromPrefab("player", "entity-1");
        
        // Then
        assertEquals(1, ecsWorld.getEntityCount());
        assertNotNull(ecsWorld.getComponent("entity-1", PositionDTO.class));
        assertNotNull(ecsWorld.getComponent("entity-1", RenderableDTO.class));
    }
}
