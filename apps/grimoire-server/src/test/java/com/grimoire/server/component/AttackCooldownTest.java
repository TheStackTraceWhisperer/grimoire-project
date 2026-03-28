package com.grimoire.server.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AttackCooldown component.
 */
class AttackCooldownTest {
    
    @Test
    void testAttackCooldownCreation() {
        AttackCooldown cooldown = new AttackCooldown(20);
        
        assertEquals(20, cooldown.ticksRemaining());
    }
    
    @Test
    void testAttackCooldownWithZeroTicks() {
        AttackCooldown cooldown = new AttackCooldown(0);
        
        assertEquals(0, cooldown.ticksRemaining());
    }
    
    @Test
    void testAttackCooldownImmutability() {
        AttackCooldown cooldown1 = new AttackCooldown(20);
        AttackCooldown cooldown2 = new AttackCooldown(19);
        
        // Verify they are different instances
        assertNotEquals(cooldown1, cooldown2);
        assertEquals(20, cooldown1.ticksRemaining());
        assertEquals(19, cooldown2.ticksRemaining());
    }
}
