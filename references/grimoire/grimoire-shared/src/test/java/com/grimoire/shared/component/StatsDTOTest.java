package com.grimoire.shared.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatsDTOTest {

    @Test
    void testStatsDTOCreation() {
        StatsDTO stats = new StatsDTO(75, 100);
        
        assertEquals(75, stats.currentHp());
        assertEquals(100, stats.maxHp());
    }

    @Test
    void testStatsDTOIsComponentDTO() {
        StatsDTO stats = new StatsDTO(50, 50);
        assertInstanceOf(ComponentDTO.class, stats);
    }

    @Test
    void testStatsDTOIsSerializable() {
        StatsDTO stats = new StatsDTO(10, 20);
        assertInstanceOf(java.io.Serializable.class, stats);
    }
}
