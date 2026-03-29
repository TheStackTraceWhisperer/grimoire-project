package com.grimoire.server.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PortalTest {
    
    @Test
    void testPortalCreation() {
        Portal portal = new Portal("zone-2", "portal-entrance");
        
        assertEquals("zone-2", portal.targetZoneId());
        assertEquals("portal-entrance", portal.targetPortalId());
    }
    
    @Test
    void testPortalIsComponent() {
        Portal portal = new Portal("zone-1", "portal-1");
        assertInstanceOf(Component.class, portal);
    }
}
