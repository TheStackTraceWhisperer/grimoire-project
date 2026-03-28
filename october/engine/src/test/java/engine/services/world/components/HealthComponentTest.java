package engine.services.world.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthComponentTest {

    @Test
    void constructor_shouldSetMaxHealthAndCurrentHealth() {
        HealthComponent health = new HealthComponent(100);
        assertEquals(100, health.getMaxHealth());
        assertEquals(100, health.getCurrentHealth());
    }

    @Test
    void constructor_shouldThrowExceptionForNonPositiveMaxHealth() {
        assertThrows(IllegalArgumentException.class, () -> new HealthComponent(0));
        assertThrows(IllegalArgumentException.class, () -> new HealthComponent(-10));
    }

    @Test
    void constructor_shouldAcceptVeryLargeMaxHealth() {
        HealthComponent health = new HealthComponent(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, health.getMaxHealth());
        assertEquals(Integer.MAX_VALUE, health.getCurrentHealth());
    }

    @Test
    void constructor_shouldAcceptMinimumValidMaxHealth() {
        HealthComponent health = new HealthComponent(1);
        assertEquals(1, health.getMaxHealth());
        assertEquals(1, health.getCurrentHealth());
    }

    @Test
    void takeDamage_shouldReduceCurrentHealth() {
        HealthComponent health = new HealthComponent(100);
        health.takeDamage(20);
        assertEquals(80, health.getCurrentHealth());
    }

    @Test
    void takeDamage_shouldNotReduceHealthBelowZero() {
        HealthComponent health = new HealthComponent(100);
        health.takeDamage(120);
        assertEquals(0, health.getCurrentHealth());
    }

    @Test
    void takeDamage_shouldNotChangeHealthForNegativeAmount() {
        HealthComponent health = new HealthComponent(100);
        health.takeDamage(-20);
        assertEquals(100, health.getCurrentHealth());
    }

    @Test
    void takeDamage_shouldHandleZeroDamage() {
        HealthComponent health = new HealthComponent(100);
        health.takeDamage(0);
        assertEquals(100, health.getCurrentHealth());
    }

    @Test
    void takeDamage_canBeCalledMultipleTimes() {
        HealthComponent health = new HealthComponent(100);
        health.takeDamage(10);
        health.takeDamage(20);
        health.takeDamage(15);
        assertEquals(55, health.getCurrentHealth());
    }

    @Test
    void takeDamage_shouldNotGoNegativeWithExcessiveDamage() {
        HealthComponent health = new HealthComponent(50);
        health.takeDamage(Integer.MAX_VALUE);
        assertEquals(0, health.getCurrentHealth());
    }

    @Test
    void isAlive_shouldReturnTrueWhenHealthIsPositive() {
        HealthComponent health = new HealthComponent(100);
        assertTrue(health.isAlive());
        health.takeDamage(99);
        assertTrue(health.isAlive());
    }

    @Test
    void isAlive_shouldReturnTrueForMinimalHealth() {
        HealthComponent health = new HealthComponent(100);
        health.takeDamage(99);
        assertTrue(health.isAlive());
        assertEquals(1, health.getCurrentHealth());
    }

    @Test
    void isAlive_shouldReturnFalseWhenHealthIsZero() {
        HealthComponent health = new HealthComponent(100);
        health.takeDamage(100);
        assertFalse(health.isAlive());
    }

    @Test
    void isAlive_shouldReturnFalseWhenHealthIsBelowZero() {
        HealthComponent health = new HealthComponent(100);
        health.takeDamage(120);
        assertFalse(health.isAlive());
    }

    @Test
    void isAlive_remainsFalseAfterDeathEvenWithNegativeDamage() {
        HealthComponent health = new HealthComponent(100);
        health.takeDamage(100);
        assertFalse(health.isAlive());

        // Trying to "heal" with negative damage should not work
        health.takeDamage(-50);
        assertFalse(health.isAlive());
        assertEquals(0, health.getCurrentHealth());
    }
}
