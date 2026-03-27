package com.grimoire.client.state;

import com.grimoire.shared.component.PositionDTO;
import com.grimoire.shared.component.RenderableDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientPrefabTest {
    
    @Test
    void testPrefabCreation() {
        ClientPrefab prefab = new ClientPrefab("TestPrefab");
        
        assertEquals("TestPrefab", prefab.getName());
        assertTrue(prefab.getComponentTemplates().isEmpty());
    }
    
    @Test
    void testAddComponent() {
        ClientPrefab prefab = new ClientPrefab("TestPrefab")
            .addComponent(new PositionDTO(10, 20))
            .addComponent(new RenderableDTO("Test", "visual-test"));
        
        assertEquals("TestPrefab", prefab.getName());
        assertEquals(2, prefab.getComponentTemplates().size());
    }
    
    @Test
    void testGetComponentTemplatesReturnsNewList() {
        ClientPrefab prefab = new ClientPrefab("TestPrefab")
            .addComponent(new PositionDTO(10, 20));
        
        var templates1 = prefab.getComponentTemplates();
        var templates2 = prefab.getComponentTemplates();
        
        assertNotSame(templates1, templates2);
        assertEquals(templates1.size(), templates2.size());
    }
    
    @Test
    void testPrefabWithMultipleComponents() {
        ClientPrefab prefab = new ClientPrefab("MonsterPrefab")
            .addComponent(new PositionDTO(0, 0))
            .addComponent(new RenderableDTO("Rat", "visual-monster-rat"));
        
        assertEquals(2, prefab.getComponentTemplates().size());
        
        // Verify components are in order
        var templates = prefab.getComponentTemplates();
        assertTrue(templates.get(0) instanceof PositionDTO);
        assertTrue(templates.get(1) instanceof RenderableDTO);
    }
}
