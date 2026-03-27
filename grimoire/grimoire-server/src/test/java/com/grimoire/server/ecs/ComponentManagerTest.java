package com.grimoire.server.ecs;

import com.grimoire.server.component.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComponentManagerTest {
    
    @Test
    void testAddAndGetComponent() {
        ComponentManager manager = new ComponentManager();
        String entityId = "entity1";
        Position position = new Position(100, 200);
        
        manager.addComponent(entityId, position);
        
        var result = manager.getComponent(entityId, Position.class);
        assertTrue(result.isPresent());
        assertEquals(100, result.get().x(), 0.001);
        assertEquals(200, result.get().y(), 0.001);
    }
    
    @Test
    void testHasComponent() {
        ComponentManager manager = new ComponentManager();
        String entityId = "entity1";
        
        assertFalse(manager.hasComponent(entityId, Position.class));
        
        manager.addComponent(entityId, new Position(0, 0));
        
        assertTrue(manager.hasComponent(entityId, Position.class));
    }
    
    @Test
    void testRemoveComponent() {
        ComponentManager manager = new ComponentManager();
        String entityId = "entity1";
        
        manager.addComponent(entityId, new Position(0, 0));
        assertTrue(manager.hasComponent(entityId, Position.class));
        
        manager.removeComponent(entityId, Position.class);
        assertFalse(manager.hasComponent(entityId, Position.class));
    }
    
    @Test
    void testRemoveAllComponents() {
        ComponentManager manager = new ComponentManager();
        String entityId = "entity1";
        
        manager.addComponent(entityId, new Position(0, 0));
        manager.addComponent(entityId, new Velocity(1, 1));
        manager.addComponent(entityId, new Zone("zone1"));
        
        manager.removeAllComponents(entityId);
        
        assertFalse(manager.hasComponent(entityId, Position.class));
        assertFalse(manager.hasComponent(entityId, Velocity.class));
        assertFalse(manager.hasComponent(entityId, Zone.class));
    }
    
    @Test
    void testGetEntitiesWithComponent() {
        ComponentManager manager = new ComponentManager();
        
        manager.addComponent("entity1", new Position(0, 0));
        manager.addComponent("entity2", new Position(10, 10));
        manager.addComponent("entity3", new Velocity(1, 1));
        
        int count = 0;
        for (String id : manager.getEntitiesWithComponent(Position.class)) {
            count++;
            assertTrue(id.equals("entity1") || id.equals("entity2"));
        }
        
        assertEquals(2, count);
    }
    
    @Test
    void testGetAllComponents() {
        ComponentManager manager = new ComponentManager();
        String entityId = "entity1";
        
        manager.addComponent(entityId, new Position(100, 200));
        manager.addComponent(entityId, new Velocity(5, 10));
        manager.addComponent(entityId, new Zone("zone1"));
        
        var components = manager.getAllComponents(entityId);
        
        assertEquals(3, components.size());
        assertTrue(components.containsKey(Position.class));
        assertTrue(components.containsKey(Velocity.class));
        assertTrue(components.containsKey(Zone.class));
    }
    
    @Test
    void testReplaceComponent() {
        ComponentManager manager = new ComponentManager();
        String entityId = "entity1";
        
        manager.addComponent(entityId, new Position(100, 100));
        manager.addComponent(entityId, new Position(200, 200));
        
        var pos = manager.getComponent(entityId, Position.class);
        assertTrue(pos.isPresent());
        assertEquals(200, pos.get().x(), 0.001);
        assertEquals(200, pos.get().y(), 0.001);
    }
}
