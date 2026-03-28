package com.grimoire.ecs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrefabTest {
    
    @Test
    void testPrefabCreation() {
        Prefab prefab = new Prefab("TestPrefab");
        
        assertEquals("TestPrefab", prefab.getName());
        assertTrue(prefab.getComponentTemplates().isEmpty());
    }
    
    @Test
    void testAddComponent() {
        Prefab prefab = new Prefab("TestPrefab")
            .addComponent(new Position(10, 20))
            .addComponent(new Stats(100, 100, 5, 10));
        
        assertEquals("TestPrefab", prefab.getName());
        assertEquals(2, prefab.getComponentTemplates().size());
    }
    
    @Test
    void testGetComponentTemplatesReturnsNewList() {
        Prefab prefab = new Prefab("TestPrefab")
            .addComponent(new Position(10, 20));
        
        var templates1 = prefab.getComponentTemplates();
        var templates2 = prefab.getComponentTemplates();
        
        assertNotSame(templates1, templates2);
        assertEquals(templates1.size(), templates2.size());
    }
    
    @Test
    void testPrefabWithMultipleComponents() {
        Prefab prefab = new Prefab("MonsterPrefab")
            .addComponent(new Position(0, 0))
            .addComponent(new Stats(50, 50, 3, 5))
            .addComponent(new Monster(Monster.MonsterType.RAT));
        
        assertEquals(3, prefab.getComponentTemplates().size());
        
        // Verify components are in order
        var templates = prefab.getComponentTemplates();
        assertTrue(templates.get(0) instanceof Position);
        assertTrue(templates.get(1) instanceof Stats);
        assertTrue(templates.get(2) instanceof Monster);
    }
}
