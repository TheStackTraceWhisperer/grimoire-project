package engine.services.input;

import engine.EngineTestHarness;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.glfw.GLFW.*;

public class InputServiceIT extends EngineTestHarness {

    @Inject
    private InputService inputService;

    @Test
    void testInitialInputState() {
        // Given: The engine has just been initialized by the harness

        // Then: The input service should report a clean, default state.
        assertFalse(inputService.isKeyPressed(GLFW_KEY_A), "Key A should not be pressed on init.");
        assertFalse(inputService.isKeyPressed(GLFW_KEY_SPACE), "Space should not be pressed on init.");
        assertFalse(inputService.isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT), "Left mouse button should not be pressed on init.");
        assertEquals(0.0, inputService.getMouseX(), "Mouse X should be at origin on init.");
        assertEquals(0.0, inputService.getMouseY(), "Mouse Y should be at origin on init.");
    }

    @Test
    void testGamepadMethodsWhenDisconnected() {
        // Given: A standard test environment where no gamepads are connected
        int gamepadPort = 0;

        // Then: The service should correctly report that no gamepad is connected
        assertFalse(inputService.isGamepadConnected(gamepadPort), "No gamepad should be connected in this test environment.");

        // And: All gamepad state queries should return default/zero values
        assertEquals(0.0f, inputService.getAxis(gamepadPort, GLFW_GAMEPAD_AXIS_LEFT_X), "Axis should be 0 for a disconnected gamepad.");
        assertFalse(inputService.isButtonPressed(gamepadPort, GLFW_GAMEPAD_BUTTON_A), "Button should not be pressed for a disconnected gamepad.");
    }
}
