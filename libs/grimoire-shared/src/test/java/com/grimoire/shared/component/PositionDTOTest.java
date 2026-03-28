package com.grimoire.shared.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionDTOTest {

    @Test
    void testPositionDTOCreation() {
        PositionDTO position = new PositionDTO(100.5, 200.3);
        
        assertEquals(100.5, position.x(), 0.001);
        assertEquals(200.3, position.y(), 0.001);
    }

    @Test
    void testPositionDTOIsComponentDTO() {
        PositionDTO position = new PositionDTO(0, 0);
        assertInstanceOf(ComponentDTO.class, position);
    }

    @Test
    void testPositionDTOIsSerializable() {
        PositionDTO position = new PositionDTO(10, 20);
        assertInstanceOf(java.io.Serializable.class, position);
    }
}
