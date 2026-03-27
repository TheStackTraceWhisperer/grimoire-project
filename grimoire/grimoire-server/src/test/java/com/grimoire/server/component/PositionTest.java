package com.grimoire.server.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {
    
    @Test
    void testPositionCreation() {
        Position position = new Position(100.5, 200.3);
        
        assertEquals(100.5, position.x(), 0.001);
        assertEquals(200.3, position.y(), 0.001);
    }
    
    @Test
    void testPositionIsComponent() {
        Position position = new Position(0, 0);
        assertInstanceOf(Component.class, position);
    }
}
