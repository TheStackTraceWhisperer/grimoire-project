package com.grimoire.shared.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PortalDTOTest {

    @Test
    void testPortalDTOCreation() {
        PortalDTO portal = new PortalDTO(20.0, 20.0);
        
        assertEquals(20.0, portal.width(), 0.001);
        assertEquals(20.0, portal.height(), 0.001);
    }

    @Test
    void testPortalDTOIsComponentDTO() {
        PortalDTO portal = new PortalDTO(10, 10);
        assertInstanceOf(ComponentDTO.class, portal);
    }

    @Test
    void testPortalDTOIsSerializable() {
        PortalDTO portal = new PortalDTO(15, 15);
        assertInstanceOf(java.io.Serializable.class, portal);
    }
}
