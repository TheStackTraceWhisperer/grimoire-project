package com.grimoire.server.content;

import com.grimoire.server.component.*;
import com.grimoire.server.ecs.ComponentManager;
import com.grimoire.server.ecs.EcsWorld;
import com.grimoire.server.ecs.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrefabRegistryTest {
    
    private EcsWorld ecsWorld;
    private PrefabRegistry prefabRegistry;
    
    @BeforeEach
    void setUp() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        ecsWorld = new EcsWorld(entityManager, componentManager);
        prefabRegistry = new PrefabRegistry(ecsWorld);
    }
    
    @Test
    void testPrefabRegistrationOnStartup() {
        // Register prefabs
        prefabRegistry.onApplicationEvent(null);
        
        // Try to create entities from registered prefabs
        String ratEntity = ecsWorld.createEntityFromPrefab("RAT");
        assertNotNull(ratEntity);
        assertTrue(ecsWorld.entityExists(ratEntity));
        
        String wolfEntity = ecsWorld.createEntityFromPrefab("WOLF");
        assertNotNull(wolfEntity);
        assertTrue(ecsWorld.entityExists(wolfEntity));
        
        String batEntity = ecsWorld.createEntityFromPrefab("BAT");
        assertNotNull(batEntity);
        assertTrue(ecsWorld.entityExists(batEntity));
        
        String skeletonEntity = ecsWorld.createEntityFromPrefab("SKELETON");
        assertNotNull(skeletonEntity);
        assertTrue(ecsWorld.entityExists(skeletonEntity));
    }
    
    @Test
    void testRatPrefabComponents() {
        prefabRegistry.onApplicationEvent(null);
        
        String entityId = ecsWorld.createEntityFromPrefab("RAT");
        
        // Verify rat has expected components
        assertTrue(ecsWorld.hasComponent(entityId, Position.class));
        assertTrue(ecsWorld.hasComponent(entityId, Velocity.class));
        assertTrue(ecsWorld.hasComponent(entityId, Renderable.class));
        assertTrue(ecsWorld.hasComponent(entityId, Stats.class));
        assertTrue(ecsWorld.hasComponent(entityId, Monster.class));
        assertTrue(ecsWorld.hasComponent(entityId, NpcAi.class));
        assertTrue(ecsWorld.hasComponent(entityId, BoundingBox.class));
        
        // Verify specific values
        assertEquals(Monster.MonsterType.RAT, ecsWorld.getComponent(entityId, Monster.class).get().type());
        assertEquals("Rat", ecsWorld.getComponent(entityId, Renderable.class).get().name());
        assertEquals(20, ecsWorld.getComponent(entityId, Stats.class).get().hp());
    }
    
    @Test
    void testWolfPrefabComponents() {
        prefabRegistry.onApplicationEvent(null);
        
        String entityId = ecsWorld.createEntityFromPrefab("WOLF");
        
        assertTrue(ecsWorld.hasComponent(entityId, Monster.class));
        assertEquals(Monster.MonsterType.WOLF, ecsWorld.getComponent(entityId, Monster.class).get().type());
        assertEquals("Wolf", ecsWorld.getComponent(entityId, Renderable.class).get().name());
        assertEquals(40, ecsWorld.getComponent(entityId, Stats.class).get().hp());
    }
    
    @Test
    void testBatPrefabComponents() {
        prefabRegistry.onApplicationEvent(null);
        
        String entityId = ecsWorld.createEntityFromPrefab("BAT");
        
        assertTrue(ecsWorld.hasComponent(entityId, Monster.class));
        assertEquals(Monster.MonsterType.BAT, ecsWorld.getComponent(entityId, Monster.class).get().type());
        assertEquals("Bat", ecsWorld.getComponent(entityId, Renderable.class).get().name());
        assertEquals(15, ecsWorld.getComponent(entityId, Stats.class).get().hp());
    }
    
    @Test
    void testSkeletonPrefabComponents() {
        prefabRegistry.onApplicationEvent(null);
        
        String entityId = ecsWorld.createEntityFromPrefab("SKELETON");
        
        assertTrue(ecsWorld.hasComponent(entityId, Monster.class));
        assertEquals(Monster.MonsterType.SKELETON, ecsWorld.getComponent(entityId, Monster.class).get().type());
        assertEquals("Skeleton", ecsWorld.getComponent(entityId, Renderable.class).get().name());
        assertEquals(60, ecsWorld.getComponent(entityId, Stats.class).get().hp());
    }
}
