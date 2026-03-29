package com.grimoire.server.ecs;

import com.grimoire.server.component.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EcsWorldTest {
    
    @Test
    void testCreateEntity() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        EcsWorld world = new EcsWorld(entityManager, componentManager);
        
        String entityId = world.createEntity();
        
        assertNotNull(entityId);
        assertTrue(world.entityExists(entityId));
    }
    
    @Test
    void testDestroyEntity() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        EcsWorld world = new EcsWorld(entityManager, componentManager);
        
        String entityId = world.createEntity();
        world.addComponent(entityId, new Position(0, 0));
        world.addComponent(entityId, new Velocity(1, 1));
        
        world.destroyEntity(entityId);
        
        assertFalse(world.entityExists(entityId));
        assertFalse(world.hasComponent(entityId, Position.class));
        assertFalse(world.hasComponent(entityId, Velocity.class));
    }
    
    @Test
    void testAddAndGetComponent() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        EcsWorld world = new EcsWorld(entityManager, componentManager);
        
        String entityId = world.createEntity();
        Position position = new Position(50, 75);
        
        world.addComponent(entityId, position);
        
        var result = world.getComponent(entityId, Position.class);
        assertTrue(result.isPresent());
        assertEquals(50, result.get().x(), 0.001);
        assertEquals(75, result.get().y(), 0.001);
    }
    
    @Test
    void testRemoveComponent() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        EcsWorld world = new EcsWorld(entityManager, componentManager);
        
        String entityId = world.createEntity();
        world.addComponent(entityId, new Position(0, 0));
        
        world.removeComponent(entityId, Position.class);
        
        assertFalse(world.hasComponent(entityId, Position.class));
    }
    
    @Test
    void testGetEntitiesWithComponent() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        EcsWorld world = new EcsWorld(entityManager, componentManager);
        
        String entity1 = world.createEntity();
        String entity2 = world.createEntity();
        String entity3 = world.createEntity();
        
        world.addComponent(entity1, new Zone("zone1"));
        world.addComponent(entity2, new Zone("zone1"));
        world.addComponent(entity3, new Velocity(1, 1));
        
        int count = 0;
        for (String id : world.getEntitiesWithComponent(Zone.class)) {
            count++;
        }
        
        assertEquals(2, count);
    }
    
    @Test
    void testTickIncrement() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        EcsWorld world = new EcsWorld(entityManager, componentManager);
        
        assertEquals(0, world.getCurrentTick());
        
        world.incrementTick();
        assertEquals(1, world.getCurrentTick());
        
        world.incrementTick();
        assertEquals(2, world.getCurrentTick());
    }
    
    @Test
    void testRegisterPrefab() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        EcsWorld world = new EcsWorld(entityManager, componentManager);
        
        Prefab prefab = new Prefab("TestPrefab")
            .addComponent(new Position(10, 20))
            .addComponent(new Velocity(1, 2));
        
        world.registerPrefab(prefab);
        
        // Should be able to create entity from prefab
        String entityId = world.createEntityFromPrefab("TestPrefab");
        assertNotNull(entityId);
        assertTrue(world.entityExists(entityId));
    }
    
    @Test
    void testCreateEntityFromPrefab() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        EcsWorld world = new EcsWorld(entityManager, componentManager);
        
        Prefab prefab = new Prefab("TestPrefab")
            .addComponent(new Position(10, 20))
            .addComponent(new Velocity(1, 2))
            .addComponent(new Stats(100, 100, 5, 10));
        
        world.registerPrefab(prefab);
        
        String entityId = world.createEntityFromPrefab("TestPrefab");
        
        assertTrue(world.hasComponent(entityId, Position.class));
        assertTrue(world.hasComponent(entityId, Velocity.class));
        assertTrue(world.hasComponent(entityId, Stats.class));
        
        assertEquals(10, world.getComponent(entityId, Position.class).get().x(), 0.001);
        assertEquals(20, world.getComponent(entityId, Position.class).get().y(), 0.001);
    }
    
    @Test
    void testCreateEntityFromPrefabWithCustomizer() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        EcsWorld world = new EcsWorld(entityManager, componentManager);
        
        Prefab prefab = new Prefab("TestPrefab")
            .addComponent(new Position(0, 0))
            .addComponent(new Velocity(1, 2));
        
        world.registerPrefab(prefab);
        
        String entityId = world.createEntityFromPrefab("TestPrefab", component -> {
            if (component instanceof Position) {
                return new Position(50, 75);
            }
            return component;
        });
        
        // Position should be customized
        assertEquals(50, world.getComponent(entityId, Position.class).get().x(), 0.001);
        assertEquals(75, world.getComponent(entityId, Position.class).get().y(), 0.001);
        
        // Velocity should remain unchanged
        assertEquals(1, world.getComponent(entityId, Velocity.class).get().dx(), 0.001);
        assertEquals(2, world.getComponent(entityId, Velocity.class).get().dy(), 0.001);
    }
    
    @Test
    void testCreateEntityFromNonExistentPrefab() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        EcsWorld world = new EcsWorld(entityManager, componentManager);
        
        assertThrows(IllegalArgumentException.class, () -> {
            world.createEntityFromPrefab("NonExistent");
        });
    }
    
    @Test
    void testCreateEntityFromPrefabWithNullName() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        EcsWorld world = new EcsWorld(entityManager, componentManager);
        
        assertThrows(NullPointerException.class, () -> {
            world.createEntityFromPrefab(null);
        });
    }
}
