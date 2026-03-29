package engine.services.world.components;

import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SpriteComponentTest {

    @Test
    void constructor_shouldSetTextureHandleAndDefaultColor() {
        SpriteComponent sprite = new SpriteComponent("test_texture");

        assertEquals("test_texture", sprite.textureHandle());
        assertEquals(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), sprite.color());
    }

    @Test
    void constructor_shouldSetTextureHandleAndCustomColor() {
        Vector4f customColor = new Vector4f(0.5f, 0.5f, 0.5f, 0.8f);
        SpriteComponent sprite = new SpriteComponent("test_texture", customColor);

        assertEquals("test_texture", sprite.textureHandle());
        assertEquals(customColor, sprite.color());
    }

    @Test
    void constructor_shouldDefaultToWhiteColorWhenNullProvided() {
        SpriteComponent sprite = new SpriteComponent("test_texture", null);

        assertEquals("test_texture", sprite.textureHandle());
        assertEquals(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), sprite.color());
    }

    @Test
    void constructor_shouldThrowExceptionWhenTextureHandleIsNull() {
        assertThrows(NullPointerException.class, () -> new SpriteComponent(null));
        assertThrows(NullPointerException.class, () -> new SpriteComponent(null, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)));
    }

    @Test
    void equals_shouldReturnTrueForIdenticalSprites() {
        Vector4f color = new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);
        SpriteComponent sprite1 = new SpriteComponent("test_texture", color);
        SpriteComponent sprite2 = new SpriteComponent("test_texture", new Vector4f(0.5f, 0.5f, 0.5f, 1.0f));

        assertEquals(sprite1, sprite2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentTextures() {
        SpriteComponent sprite1 = new SpriteComponent("texture1");
        SpriteComponent sprite2 = new SpriteComponent("texture2");

        assertNotEquals(sprite1, sprite2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentColors() {
        SpriteComponent sprite1 = new SpriteComponent("test_texture", new Vector4f(1.0f, 0.0f, 0.0f, 1.0f));
        SpriteComponent sprite2 = new SpriteComponent("test_texture", new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));

        assertNotEquals(sprite1, sprite2);
    }

    @Test
    void hashCode_shouldBeConsistentForEqualSprites() {
        Vector4f color = new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);
        SpriteComponent sprite1 = new SpriteComponent("test_texture", color);
        SpriteComponent sprite2 = new SpriteComponent("test_texture", new Vector4f(0.5f, 0.5f, 0.5f, 1.0f));

        assertEquals(sprite1.hashCode(), sprite2.hashCode());
    }
}
