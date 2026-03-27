package com.grimoire.shared.dto;

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
    void testMovementIntentIsSerializable() {
        MovementIntent intent = new MovementIntent(0, 0);
        assertInstanceOf(java.io.Serializable.class, intent);
    }
}
