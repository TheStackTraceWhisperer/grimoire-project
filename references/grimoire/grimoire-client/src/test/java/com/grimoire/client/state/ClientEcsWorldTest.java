package com.grimoire.client.state;

import com.grimoire.shared.component.PositionDTO;
import com.grimoire.shared.component.RenderableDTO;
import com.grimoire.shared.dto.EntityDespawn;
import com.grimoire.shared.dto.EntitySpawn;
import com.grimoire.shared.dto.GameStateUpdate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClientEcsWorldTest {
    
    @Test
    void testSpawnEntity() {
        ClientEcsWorld world = new ClientEcsWorld();
        
        EntitySpawn spawn = new EntitySpawn("entity1", List.of(
                new PositionDTO(100, 200),
                new RenderableDTO("Player", "visual-player")
        ));
        
        world.spawnEntity(spawn);
        
        assertTrue(world.getEntities().containsKey("entity1"));
        assertEquals(2, world.getEntities().get("entity1").size());
    }
    
    @Test
    void testDespawnEntity() {
        ClientEcsWorld world = new ClientEcsWorld();
        
        EntitySpawn spawn = new EntitySpawn("entity1", List.of(
                new PositionDTO(100, 200)
        ));
        world.spawnEntity(spawn);
        
        EntityDespawn despawn = new EntityDespawn("entity1");
        world.despawnEntity(despawn);
        
        assertFalse(world.getEntities().containsKey("entity1"));
    }
    
    @Test
    void testProcessStateUpdate() {
        ClientEcsWorld world = new ClientEcsWorld();
        
        // Spawn initial entity
        EntitySpawn spawn = new EntitySpawn("entity1", List.of(
                new PositionDTO(100, 200)
        ));
        world.spawnEntity(spawn);
        
        // Update position
        GameStateUpdate update = new GameStateUpdate(1L, Map.of(
                "entity1", List.of(new PositionDTO(150, 250))
        ));
        world.processStateUpdate(update);
        
        var entity = world.getEntities().get("entity1");
        PositionDTO pos = (PositionDTO) entity.get(PositionDTO.class);
        assertEquals(150, pos.x(), 0.001);
        assertEquals(250, pos.y(), 0.001);
    }
    
    @Test
    void testClearAllEntities() {
        ClientEcsWorld world = new ClientEcsWorld();
        
        world.spawnEntity(new EntitySpawn("entity1", List.of(new PositionDTO(0, 0))));
        world.spawnEntity(new EntitySpawn("entity2", List.of(new PositionDTO(10, 10))));
        
        assertEquals(2, world.getEntities().size());
        
        world.clearAllEntities();
        
        assertEquals(0, world.getEntities().size());
    }
    
    @Test
    void testLocalPlayerEntityId() {
        ClientEcsWorld world = new ClientEcsWorld();
        
        world.setLocalPlayerEntityId("player123");
        
        assertEquals("player123", world.getLocalPlayerEntityId());
    }
    
    @Test
    void testCurrentZone() {
        ClientEcsWorld world = new ClientEcsWorld();
        
        world.setCurrentZone("zone1");
        
        assertEquals("zone1", world.getCurrentZone());
    }
    
    @Test
    void testRegisterPrefab() {
        ClientEcsWorld world = new ClientEcsWorld();
        
        ClientPrefab prefab = new ClientPrefab("TestPrefab")
            .addComponent(new PositionDTO(10, 20))
            .addComponent(new RenderableDTO("Test", "visual-test"));
        
        world.registerPrefab(prefab);
        
        // Should be able to create entity from prefab
        String entityId = world.createEntityFromPrefab("TestPrefab", "entity1");
        assertEquals("entity1", entityId);
        assertTrue(world.getEntities().containsKey("entity1"));
    }
    
    @Test
    void testCreateEntityFromPrefab() {
        ClientEcsWorld world = new ClientEcsWorld();
        
        ClientPrefab prefab = new ClientPrefab("TestPrefab")
            .addComponent(new PositionDTO(10, 20))
            .addComponent(new RenderableDTO("Test", "visual-test"));
        
        world.registerPrefab(prefab);
        
        String entityId = world.createEntityFromPrefab("TestPrefab", "entity1");
        
        var entity = world.getEntities().get(entityId);
        assertNotNull(entity);
        assertEquals(2, entity.size());
        
        PositionDTO pos = (PositionDTO) entity.get(PositionDTO.class);
        assertEquals(10, pos.x(), 0.001);
        assertEquals(20, pos.y(), 0.001);
        
        RenderableDTO renderable = (RenderableDTO) entity.get(RenderableDTO.class);
        assertEquals("Test", renderable.name());
    }
    
    @Test
    void testCreateEntityFromPrefabWithCustomizer() {
        ClientEcsWorld world = new ClientEcsWorld();
        
        ClientPrefab prefab = new ClientPrefab("TestPrefab")
            .addComponent(new PositionDTO(0, 0))
            .addComponent(new RenderableDTO("Test", "visual-test"));
        
        world.registerPrefab(prefab);
        
        String entityId = world.createEntityFromPrefab("TestPrefab", "entity1", component -> {
            if (component instanceof PositionDTO) {
                return new PositionDTO(50, 75);
            }
            return component;
        });
        
        var entity = world.getEntities().get(entityId);
        
        // Position should be customized
        PositionDTO pos = (PositionDTO) entity.get(PositionDTO.class);
        assertEquals(50, pos.x(), 0.001);
        assertEquals(75, pos.y(), 0.001);
        
        // Renderable should remain unchanged
        RenderableDTO renderable = (RenderableDTO) entity.get(RenderableDTO.class);
        assertEquals("Test", renderable.name());
    }
    
    @Test
    void testCreateEntityFromNonExistentPrefab() {
        ClientEcsWorld world = new ClientEcsWorld();
        
        assertThrows(IllegalArgumentException.class, () -> {
            world.createEntityFromPrefab("NonExistent", "entity1");
        });
    }
    
    @Test
    void testCreateEntityFromPrefabWithNullPrefabName() {
        ClientEcsWorld world = new ClientEcsWorld();
        
        assertThrows(NullPointerException.class, () -> {
            world.createEntityFromPrefab(null, "entity1");
        });
    }
    
    @Test
    void testCreateEntityFromPrefabWithNullEntityId() {
        ClientEcsWorld world = new ClientEcsWorld();
        
        ClientPrefab prefab = new ClientPrefab("TestPrefab")
            .addComponent(new PositionDTO(10, 20));
        world.registerPrefab(prefab);
        
        assertThrows(NullPointerException.class, () -> {
            world.createEntityFromPrefab("TestPrefab", null);
        });
    }
}
