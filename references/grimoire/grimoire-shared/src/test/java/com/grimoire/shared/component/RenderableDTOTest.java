package com.grimoire.shared.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RenderableDTOTest {

    @Test
    void testRenderableDTOCreation() {
        RenderableDTO renderable = new RenderableDTO("Player", "visual-player");
        
        assertEquals("Player", renderable.name());
        assertEquals("visual-player", renderable.visualId());
    }

    @Test
    void testRenderableDTOIsComponentDTO() {
        RenderableDTO renderable = new RenderableDTO("Test", "visual-test");
        assertInstanceOf(ComponentDTO.class, renderable);
    }

    @Test
    void testRenderableDTOIsSerializable() {
        RenderableDTO renderable = new RenderableDTO("NPC", "visual-npc");
        assertInstanceOf(java.io.Serializable.class, renderable);
    }
}
