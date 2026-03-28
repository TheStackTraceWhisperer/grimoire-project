package engine.services.world.components;

import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UIImageComponentTest {

    @Test
    void constructor_shouldSetTextureHandleAndColor() {
        Vector4f customColor = new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);
        UIImageComponent image = new UIImageComponent("test_texture", customColor);

        assertEquals("test_texture", image.textureHandle);
        assertEquals(customColor, image.color);
    }

    @Test
    void constructor_shouldUseDefaultWhiteColorWhenNullProvided() {
        UIImageComponent image = new UIImageComponent("test_texture", null);

        assertEquals("test_texture", image.textureHandle);
        assertEquals(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), image.color);
    }

    @Test
    void constructor_withOnlyTextureHandle_shouldUseDefaultColor() {
        UIImageComponent image = new UIImageComponent("test_texture");

        assertEquals("test_texture", image.textureHandle);
        assertEquals(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), image.color);
    }

    @Test
    void constructor_shouldAcceptNullTextureHandle() {
        UIImageComponent image = new UIImageComponent(null);

        assertNull(image.textureHandle);
        assertEquals(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), image.color);
    }

    @Test
    void textureHandle_canBeModified() {
        UIImageComponent image = new UIImageComponent("original_texture");

        image.textureHandle = "new_texture";

        assertEquals("new_texture", image.textureHandle);
    }

    @Test
    void color_shouldBeImmutableReference() {
        Vector4f color = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f);
        UIImageComponent image = new UIImageComponent("test_texture", color);

        // Color is final, so reference cannot be changed
        assertEquals(color, image.color);
    }
}
