package com.grimoire.server.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoundingBoxTest {
    
    @Test
    void testBoundingBoxCreation() {
        BoundingBox boundingBox = new BoundingBox(10.0, 20.0);
        
        assertEquals(10.0, boundingBox.width(), 0.001);
        assertEquals(20.0, boundingBox.height(), 0.001);
    }
    
    @Test
    void testBoundingBoxIsComponent() {
        BoundingBox boundingBox = new BoundingBox(8.0, 8.0);
        assertInstanceOf(Component.class, boundingBox);
    }
    
    @Test
    void testBoundingBoxWithZeroDimensions() {
        BoundingBox boundingBox = new BoundingBox(0, 0);
        assertEquals(0, boundingBox.width());
        assertEquals(0, boundingBox.height());
    }
}
