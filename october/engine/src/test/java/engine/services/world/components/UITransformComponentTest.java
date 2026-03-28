package engine.services.world.components;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UITransformComponentTest {

    @Test
    void defaultConstructor_shouldInitializeWithDefaultValues() {
        UITransformComponent transform = new UITransformComponent();

        assertEquals(new Vector2f(0.5f, 0.5f), transform.anchor);
        assertEquals(new Vector2f(0.5f, 0.5f), transform.pivot);
        assertEquals(new Vector2f(100.0f, 100.0f), transform.size);
        assertEquals(new Vector3f(0.0f, 0.0f, 0.0f), transform.offset);
        assertFalse(transform.relativeSize);
        assertArrayEquals(new float[4], transform.screenBounds);
    }

    @Test
    void constructor_shouldSetProvidedValues() {
        Vector2f anchor = new Vector2f(0.0f, 1.0f);
        Vector2f pivot = new Vector2f(1.0f, 0.0f);
        Vector2f size = new Vector2f(200.0f, 50.0f);
        Vector3f offset = new Vector3f(10.0f, 20.0f, 5.0f);

        UITransformComponent transform = new UITransformComponent(anchor, pivot, size, offset);

        assertEquals(anchor, transform.anchor);
        assertEquals(pivot, transform.pivot);
        assertEquals(size, transform.size);
        assertEquals(offset, transform.offset);
    }

    @Test
    void constructor_shouldUseDefaultsForNullAnchor() {
        UITransformComponent transform = new UITransformComponent(
            null,
            new Vector2f(0.0f, 0.0f),
            new Vector2f(100.0f, 100.0f),
            new Vector3f(0.0f, 0.0f, 0.0f)
        );

        assertEquals(new Vector2f(0.5f, 0.5f), transform.anchor);
    }

    @Test
    void constructor_shouldUseDefaultsForNullPivot() {
        UITransformComponent transform = new UITransformComponent(
            new Vector2f(0.0f, 0.0f),
            null,
            new Vector2f(100.0f, 100.0f),
            new Vector3f(0.0f, 0.0f, 0.0f)
        );

        assertEquals(new Vector2f(0.5f, 0.5f), transform.pivot);
    }

    @Test
    void constructor_shouldUseDefaultsForNullSize() {
        UITransformComponent transform = new UITransformComponent(
            new Vector2f(0.5f, 0.5f),
            new Vector2f(0.5f, 0.5f),
            null,
            new Vector3f(0.0f, 0.0f, 0.0f)
        );

        assertEquals(new Vector2f(100.0f, 30.0f), transform.size);
    }

    @Test
    void constructor_shouldUseDefaultsForNullOffset() {
        UITransformComponent transform = new UITransformComponent(
            new Vector2f(0.5f, 0.5f),
            new Vector2f(0.5f, 0.5f),
            new Vector2f(100.0f, 100.0f),
            null
        );

        assertEquals(new Vector3f(0.0f, 0.0f, 0.0f), transform.offset);
    }

    @Test
    void constructor_shouldUseDefaultsForAllNullValues() {
        UITransformComponent transform = new UITransformComponent(null, null, null, null);

        assertEquals(new Vector2f(0.5f, 0.5f), transform.anchor);
        assertEquals(new Vector2f(0.5f, 0.5f), transform.pivot);
        assertEquals(new Vector2f(100.0f, 30.0f), transform.size);
        assertEquals(new Vector3f(0.0f, 0.0f, 0.0f), transform.offset);
    }

    @Test
    void relativeSize_canBeModified() {
        UITransformComponent transform = new UITransformComponent();

        transform.relativeSize = true;
        assertTrue(transform.relativeSize);

        transform.relativeSize = false;
        assertFalse(transform.relativeSize);
    }

    @Test
    void screenBounds_shouldBeInitializedWithZeros() {
        UITransformComponent transform = new UITransformComponent();

        assertArrayEquals(new float[]{0.0f, 0.0f, 0.0f, 0.0f}, transform.screenBounds);
    }

    @Test
    void screenBounds_canBeModified() {
        UITransformComponent transform = new UITransformComponent();

        transform.screenBounds[0] = 10.0f;
        transform.screenBounds[1] = 20.0f;
        transform.screenBounds[2] = 100.0f;
        transform.screenBounds[3] = 50.0f;

        assertArrayEquals(new float[]{10.0f, 20.0f, 100.0f, 50.0f}, transform.screenBounds);
    }
}
