package engine.services.world.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovementStatsComponentTest {

    @Test
    void constructor_shouldSetSpeed() {
        MovementStatsComponent stats = new MovementStatsComponent(5.0f);
        assertEquals(5.0f, stats.speed());
    }

    @Test
    void constructor_shouldAcceptZeroSpeed() {
        MovementStatsComponent stats = new MovementStatsComponent(0.0f);
        assertEquals(0.0f, stats.speed());
    }

    @Test
    void constructor_shouldAcceptNegativeSpeed() {
        // While negative speed might not make logical sense, the component doesn't validate it
        MovementStatsComponent stats = new MovementStatsComponent(-5.0f);
        assertEquals(-5.0f, stats.speed());
    }

    @Test
    void constructor_shouldAcceptVeryLargeSpeed() {
        float largeSpeed = Float.MAX_VALUE;
        MovementStatsComponent stats = new MovementStatsComponent(largeSpeed);
        assertEquals(largeSpeed, stats.speed());
    }

    @Test
    void equals_shouldReturnTrueForSameSpeed() {
        MovementStatsComponent stats1 = new MovementStatsComponent(100.0f);
        MovementStatsComponent stats2 = new MovementStatsComponent(100.0f);

        assertEquals(stats1, stats2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentSpeed() {
        MovementStatsComponent stats1 = new MovementStatsComponent(100.0f);
        MovementStatsComponent stats2 = new MovementStatsComponent(50.0f);

        assertNotEquals(stats1, stats2);
    }

    @Test
    void hashCode_shouldBeConsistentForEqualComponents() {
        MovementStatsComponent stats1 = new MovementStatsComponent(100.0f);
        MovementStatsComponent stats2 = new MovementStatsComponent(100.0f);

        assertEquals(stats1.hashCode(), stats2.hashCode());
    }

    @Test
    void toString_shouldContainSpeed() {
        MovementStatsComponent stats = new MovementStatsComponent(75.5f);

        String toString = stats.toString();
        assertTrue(toString.contains("75.5"));
    }
}
