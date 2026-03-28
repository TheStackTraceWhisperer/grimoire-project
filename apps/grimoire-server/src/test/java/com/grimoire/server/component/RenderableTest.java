package com.grimoire.server.component;

import com.grimoire.ecs.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RenderableTest {
    
    @Test
    void testRenderableCreation() {
        Renderable renderable = new Renderable("Rat", "visual-monster-rat");
        
        assertEquals("Rat", renderable.name());
        assertEquals("visual-monster-rat", renderable.visualId());
    }
    
    @Test
    void testRenderableIsComponent() {
        Renderable renderable = new Renderable("Test", "visual-test");
        assertInstanceOf(Component.class, renderable);
    }
}
