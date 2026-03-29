package com.grimoire.server.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovementIntentTest {
    
    @Test
    void testMovementIntentCreation() {
        MovementIntent intent = new MovementIntent(5.0, -3.0);
        
        assertEquals(5.0, intent.targetX(), 0.001);
        assertEquals(-3.0, intent.targetY(), 0.001);
    }
    
    @Test
    void testMovementIntentIsComponent() {
        MovementIntent intent = new MovementIntent(0, 0);
        assertInstanceOf(Component.class, intent);
    }
    
    @Test
    void testMovementIntentWithZeroValues() {
        MovementIntent intent = new MovementIntent(0, 0);
        assertEquals(0, intent.targetX());
        assertEquals(0, intent.targetY());
    }
}
