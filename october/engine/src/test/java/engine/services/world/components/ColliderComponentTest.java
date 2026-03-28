package engine.services.world.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColliderComponentTest {

    // A mock implementation of the ColliderType for testing purposes
    private enum TestColliderType implements ColliderComponent.ColliderType {
        PLAYER, ENEMY, WALL, PROJECTILE
    }

    @Test
    void constructor_shouldSetAllFields() {
        ColliderComponent.ColliderType type = TestColliderType.PLAYER;
        int width = 32;
        int height = 32;
        int offsetX = 0;
        int offsetY = 0;

        ColliderComponent collider = new ColliderComponent(type, width, height, offsetX, offsetY);

        assertEquals(type, collider.getType());
        assertEquals(width, collider.getWidth());
        assertEquals(height, collider.getHeight());
        assertEquals(offsetX, collider.getOffsetX());
        assertEquals(offsetY, collider.getOffsetY());
    }

    @Test
    void constructor_shouldAcceptNegativeOffsets() {
        ColliderComponent collider = new ColliderComponent(
            TestColliderType.PLAYER,
            32,
            32,
            -5,
            -10
        );

        assertEquals(-5, collider.getOffsetX());
        assertEquals(-10, collider.getOffsetY());
    }

    @Test
    void constructor_shouldAcceptZeroDimensions() {
        // While not practical, the component doesn't validate dimensions
        ColliderComponent collider = new ColliderComponent(
            TestColliderType.WALL,
            0,
            0,
            0,
            0
        );

        assertEquals(0, collider.getWidth());
        assertEquals(0, collider.getHeight());
    }

    @Test
    void constructor_shouldAcceptVariousDimensions() {
        ColliderComponent smallCollider = new ColliderComponent(TestColliderType.PROJECTILE, 8, 8, 0, 0);
        ColliderComponent largeCollider = new ColliderComponent(TestColliderType.ENEMY, 128, 256, 10, 20);

        assertEquals(8, smallCollider.getWidth());
        assertEquals(8, smallCollider.getHeight());
        assertEquals(128, largeCollider.getWidth());
        assertEquals(256, largeCollider.getHeight());
    }

    @Test
    void getters_shouldReturnCorrectValues() {
        ColliderComponent collider = new ColliderComponent(
            TestColliderType.PLAYER,
            48,
            64,
            -8,
            -16
        );

        assertEquals(TestColliderType.PLAYER, collider.getType());
        assertEquals(48, collider.getWidth());
        assertEquals(64, collider.getHeight());
        assertEquals(-8, collider.getOffsetX());
        assertEquals(-16, collider.getOffsetY());
    }

    @Test
    void constructor_shouldAcceptNullType() {
        // Testing with null type
        ColliderComponent collider = new ColliderComponent(null, 32, 32, 0, 0);

        assertNull(collider.getType());
        assertEquals(32, collider.getWidth());
        assertEquals(32, collider.getHeight());
    }

    @Test
    void constructor_shouldAcceptLargeValues() {
        ColliderComponent collider = new ColliderComponent(
            TestColliderType.WALL,
            Integer.MAX_VALUE,
            Integer.MAX_VALUE,
            Integer.MAX_VALUE,
            Integer.MIN_VALUE
        );

        assertEquals(Integer.MAX_VALUE, collider.getWidth());
        assertEquals(Integer.MAX_VALUE, collider.getHeight());
        assertEquals(Integer.MAX_VALUE, collider.getOffsetX());
        assertEquals(Integer.MIN_VALUE, collider.getOffsetY());
    }
}
