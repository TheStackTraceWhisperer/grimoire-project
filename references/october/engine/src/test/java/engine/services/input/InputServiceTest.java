package engine.services.input;

import engine.game.GameAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.joml.Vector2d;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.glfw.GLFW.*;

@ExtendWith(MockitoExtension.class)
class InputServiceTest {

  @Mock
  private engine.services.window.WindowService windowService;

  private InputService input;

  @BeforeEach
  void setUp() {
    input = new InputService(windowService);
  }

  // --- Reflection helpers to set private state ---
  private void setBooleanArray(String fieldName, int index, boolean value) throws Exception {
    Field f = InputService.class.getDeclaredField(fieldName);
    f.setAccessible(true);
    boolean[] arr = (boolean[]) f.get(input);
    arr[index] = value;
  }

  private void setDoubleField(String fieldName, double value) throws Exception {
    Field f = InputService.class.getDeclaredField(fieldName);
    f.setAccessible(true);
    f.setDouble(input, value);
  }

  @Test
  void isKeyPressed_and_isMouseButtonPressed_handle_invalid_indices() {
    assertFalse(input.isKeyPressed(-1));
    assertFalse(input.isKeyPressed(GLFW_KEY_LAST + 1));
    assertFalse(input.isMouseButtonPressed(-1));
    assertFalse(input.isMouseButtonPressed(GLFW_MOUSE_BUTTON_LAST + 1));
  }

  @Test
  void justPressed_detection_for_keys_and_mouse() throws Exception {
    int key = GLFW_KEY_A;
    int btn = GLFW_MOUSE_BUTTON_LEFT;

    // Initially not pressed
    assertFalse(input.isKeyJustPressed(key));
    assertFalse(input.isMouseButtonJustPressed(btn));

    // Press this frame, last frame false -> just pressed
    setBooleanArray("keys", key, true);
    setBooleanArray("keysLastFrame", key, false);
    setBooleanArray("mouseButtons", btn, true);
    setBooleanArray("mouseButtonsLastFrame", btn, false);

    assertTrue(input.isKeyJustPressed(key));
    assertTrue(input.isMouseButtonJustPressed(btn));

    // After update(), lastFrame mirrors current -> not just pressed next frame
    input.update();
    assertFalse(input.isKeyJustPressed(key));
    assertFalse(input.isMouseButtonJustPressed(btn));
  }

  @Test
  void isActionJustPressed_maps_all_actions() throws Exception {
    // Mark all mapped keys as pressed this frame but not last frame
    int[] keys = new int[] {
      GLFW_KEY_W, // MOVE_UP
      GLFW_KEY_S, // MOVE_DOWN
      GLFW_KEY_A, // MOVE_LEFT
      GLFW_KEY_D, // MOVE_RIGHT
      GLFW_KEY_SPACE, // ATTACK
      GLFW_KEY_E, // INTERACT
      GLFW_KEY_ESCAPE // OPEN_MENU
    };
    GameAction[] actions = GameAction.values();

    for (int i = 0; i < actions.length; i++) {
      setBooleanArray("keys", keys[i], true);
      setBooleanArray("keysLastFrame", keys[i], false);
      assertTrue(input.isActionJustPressed(actions[i]), "Expected just pressed for action " + actions[i]);
    }

    // After update, none should be just-pressed anymore without state change
    input.update();
    for (GameAction action : actions) {
      assertFalse(input.isActionJustPressed(action));
    }
  }

  @Test
  void getCursorPos_and_clear_resets_state() throws Exception {
    setDoubleField("mouseX", 42.5);
    setDoubleField("mouseY", -13.0);

    Vector2d pos = input.getCursorPos();
    assertEquals(42.5, pos.x(), 1e-6);
    assertEquals(-13.0, pos.y(), 1e-6);

    // Also set some key/button then clear
    setBooleanArray("keys", GLFW_KEY_SPACE, true);
    setBooleanArray("mouseButtons", GLFW_MOUSE_BUTTON_RIGHT, true);
    input.clear();

    assertFalse(input.isKeyPressed(GLFW_KEY_SPACE));
    assertFalse(input.isMouseButtonPressed(GLFW_MOUSE_BUTTON_RIGHT));
    assertEquals(0.0, input.getCursorPos().x(), 1e-6);
    assertEquals(0.0, input.getCursorPos().y(), 1e-6);
  }

  @Test
  void gamepad_invalid_ports_and_defaults() {
    assertFalse(input.isGamepadConnected(-1));
    assertFalse(input.isGamepadConnected(100));
    // On disconnected/default environment, axis/button should be default values
    assertEquals(0.0f, input.getAxis(0, GLFW_GAMEPAD_AXIS_LEFT_X), 1e-6);
    assertFalse(input.isButtonPressed(0, GLFW_GAMEPAD_BUTTON_A));
  }
}
