package com.grimoire.server.ecs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityManagerTest {
    
    @Test
    void testCreateEntity() {
        EntityManager manager = new EntityManager();
        String entityId = manager.createEntity();
        
        assertNotNull(entityId);
        assertTrue(manager.exists(entityId));
    }
    
    @Test
    void testDestroyEntity() {
        EntityManager manager = new EntityManager();
        String entityId = manager.createEntity();
        
        manager.destroyEntity(entityId);
        
        assertFalse(manager.exists(entityId));
    }
    
    @Test
    void testGetAllEntityIds() {
        EntityManager manager = new EntityManager();
        String entity1 = manager.createEntity();
        String entity2 = manager.createEntity();
        
        int count = 0;
        for (String id : manager.getAllEntityIds()) {
            count++;
        }
        
        assertEquals(2, count);
    }
}
