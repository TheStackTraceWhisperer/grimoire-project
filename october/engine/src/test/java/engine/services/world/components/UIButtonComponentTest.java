package engine.services.world.components;

import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UIButtonComponentTest {

    @Test
    void constructor_shouldSetAllFieldsWithProvidedValues() {
        Vector4f normalColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        Vector4f hoveredColor = new Vector4f(0.8f, 0.8f, 0.8f, 1.0f);
        Vector4f pressedColor = new Vector4f(0.6f, 0.6f, 0.6f, 1.0f);

        UIButtonComponent button = new UIButtonComponent(
            "START_GAME",
            "button_normal.png",
            "button_hover.png",
            "button_pressed.png",
            normalColor,
            hoveredColor,
            pressedColor
        );

        assertEquals("START_GAME", button.actionEvent);
        assertEquals("button_normal.png", button.normalTexture);
        assertEquals("button_hover.png", button.hoveredTexture);
        assertEquals("button_pressed.png", button.pressedTexture);
        assertEquals(normalColor, button.normalColor);
        assertEquals(hoveredColor, button.hoveredColor);
        assertEquals(pressedColor, button.pressedColor);
        assertEquals(UIButtonComponent.ButtonState.NORMAL, button.currentState);
    }

    @Test
    void constructor_shouldUseDefaultColorsWhenNullProvided() {
        UIButtonComponent button = new UIButtonComponent(
            "TEST_ACTION",
            "normal.png",
            "hover.png",
            "pressed.png",
            null,
            null,
            null
        );

        assertEquals(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), button.normalColor);
        assertEquals(new Vector4f(0.9f, 0.9f, 0.9f, 1.0f), button.hoveredColor);
        assertEquals(new Vector4f(0.7f, 0.7f, 0.7f, 1.0f), button.pressedColor);
    }

    @Test
    void constructor_shouldAllowPartialNullColors() {
        Vector4f customNormal = new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);

        UIButtonComponent button = new UIButtonComponent(
            "ACTION",
            "normal.png",
            "hover.png",
            "pressed.png",
            customNormal,
            null,
            null
        );

        assertEquals(customNormal, button.normalColor);
        assertEquals(new Vector4f(0.9f, 0.9f, 0.9f, 1.0f), button.hoveredColor);
        assertEquals(new Vector4f(0.7f, 0.7f, 0.7f, 1.0f), button.pressedColor);
    }

    @Test
    void buttonState_shouldDefaultToNormal() {
        UIButtonComponent button = new UIButtonComponent(
            "ACTION",
            "normal.png",
            "hover.png",
            "pressed.png",
            null,
            null,
            null
        );

        assertEquals(UIButtonComponent.ButtonState.NORMAL, button.currentState);
    }

    @Test
    void buttonState_canBeChanged() {
        UIButtonComponent button = new UIButtonComponent(
            "ACTION",
            "normal.png",
            "hover.png",
            "pressed.png",
            null,
            null,
            null
        );

        button.currentState = UIButtonComponent.ButtonState.HOVERED;
        assertEquals(UIButtonComponent.ButtonState.HOVERED, button.currentState);

        button.currentState = UIButtonComponent.ButtonState.PRESSED;
        assertEquals(UIButtonComponent.ButtonState.PRESSED, button.currentState);

        button.currentState = UIButtonComponent.ButtonState.NORMAL;
        assertEquals(UIButtonComponent.ButtonState.NORMAL, button.currentState);
    }

    @Test
    void constructor_shouldAcceptNullActionEvent() {
        UIButtonComponent button = new UIButtonComponent(
            null,
            "normal.png",
            "hover.png",
            "pressed.png",
            null,
            null,
            null
        );

        assertNull(button.actionEvent);
    }

    @Test
    void constructor_shouldAcceptNullTextureHandles() {
        UIButtonComponent button = new UIButtonComponent(
            "ACTION",
            null,
            null,
            null,
            null,
            null,
            null
        );

        assertNull(button.normalTexture);
        assertNull(button.hoveredTexture);
        assertNull(button.pressedTexture);
    }
}
