package engine.services.input;

import engine.IService;
import engine.game.GameAction;
import engine.services.window.WindowService;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import java.util.Arrays;

@Singleton
@RequiredArgsConstructor
public class InputService implements IService {
  private final WindowService windowService;

  private static final int MAX_GAMEPADS = 8;

  private final boolean[] keys = new boolean[GLFW.GLFW_KEY_LAST + 1];
  private final boolean[] keysLastFrame = new boolean[GLFW.GLFW_KEY_LAST + 1];
  private final boolean[] mouseButtons = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST + 1];
  private final boolean[] mouseButtonsLastFrame = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST + 1];

  // Track gamepad button states per port for "just pressed" detection
  private final byte[][] gamepadButtons = new byte[MAX_GAMEPADS][GLFW.GLFW_GAMEPAD_BUTTON_LAST + 1];
  private final byte[][] gamepadButtonsLastFrame = new byte[MAX_GAMEPADS][GLFW.GLFW_GAMEPAD_BUTTON_LAST + 1];

  @Getter
  private double mouseX;
  @Getter
  private double mouseY;

  @Override
  public int executionOrder() {
    return 15;
  }

  /** Install GLFW callbacks to capture input events. */
  @Override
  public void start() {
    GLFW.glfwSetKeyCallback(windowService.getHandle(), new GLFWKeyCallback() {
      @Override
      public void invoke(long window, int key, int scancode, int action, int mods) {
        if (key >= 0 && key <= GLFW.GLFW_KEY_LAST) {
          keys[key] = (action != GLFW.GLFW_RELEASE);
        }
      }
    });

    GLFW.glfwSetMouseButtonCallback(windowService.getHandle(), new GLFWMouseButtonCallback() {
      @Override
      public void invoke(long window, int button, int action, int mods) {
        if (button >= 0 && button <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
          mouseButtons[button] = (action != GLFW.GLFW_RELEASE);
        }
      }
    });

    GLFW.glfwSetCursorPosCallback(windowService.getHandle(), new GLFWCursorPosCallback() {
      @Override
      public void invoke(long window, double xpos, double ypos) {
        mouseX = xpos;
        mouseY = ypos;
      }
    });
  }

  /** Clear all input states. */
  public void clear() {
    Arrays.fill(keys, false);
    Arrays.fill(mouseButtons, false);
    mouseX = 0.0;
    mouseY = 0.0;
  }

  public boolean isKeyPressed(int keyCode) {
    if (keyCode < 0 || keyCode > GLFW.GLFW_KEY_LAST) {
      return false;
    }
    return keys[keyCode];
  }

  public boolean isMouseButtonPressed(int button) {
    if (button < 0 || button > GLFW.GLFW_MOUSE_BUTTON_LAST) {
      return false;
    }
    return mouseButtons[button];
  }

  /** true if any key is pressed. */
  public boolean isAnyKeyPressed() {
    for (int i = 0; i <= GLFW.GLFW_KEY_LAST; i++) {
      if (keys[i]) return true;
    }
    return false;
  }

  /** true if any mouse button is pressed. */
  public boolean isAnyMouseButtonPressed() {
    for (int i = 0; i <= GLFW.GLFW_MOUSE_BUTTON_LAST; i++) {
      if (mouseButtons[i]) return true;
    }
    return false;
  }

  /** true if any gamepad button is pressed. */
  public boolean isAnyGamepadButtonPressed() {
    for (int port = 0; port < MAX_GAMEPADS; port++) {
      if (!isGamepadConnected(port)) continue;
      for (int b = 0; b <= GLFW.GLFW_GAMEPAD_BUTTON_LAST; b++) {
        if (gamepadButtons[port][b] == GLFW.GLFW_PRESS) { return true; }
      }
    }
    return false;
  }

  /** true if any key was just pressed this frame. */
  public boolean isAnyKeyJustPressed() {
    for (int i = 0; i <= GLFW.GLFW_KEY_LAST; i++) {
      if (keys[i] && !keysLastFrame[i]) return true;
    }
    return false;
  }

  /** true if any mouse button was just pressed this frame. */
  public boolean isAnyMouseButtonJustPressed() {
    for (int i = 0; i <= GLFW.GLFW_MOUSE_BUTTON_LAST; i++) {
      if (mouseButtons[i] && !mouseButtonsLastFrame[i]) return true;
    }
    return false;
  }

  /** true if any gamepad button was just pressed this frame. */
  public boolean isAnyGamepadButtonJustPressed() {
    for (int port = 0; port < MAX_GAMEPADS; port++) {
      if (!isGamepadConnected(port)) continue;
      for (int b = 0; b <= GLFW.GLFW_GAMEPAD_BUTTON_LAST; b++) {
        if (gamepadButtons[port][b] == GLFW.GLFW_PRESS && gamepadButtonsLastFrame[port][b] == GLFW.GLFW_RELEASE) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isGamepadConnected(int port) {
    if (port < 0 || port >= MAX_GAMEPADS) {
      return false;
    }

    int joystickId = GLFW.GLFW_JOYSTICK_1 + port;
    return GLFW.glfwJoystickPresent(joystickId) && GLFW.glfwJoystickIsGamepad(joystickId);
  }

  public float getAxis(int port, int axis) {
    if (!isGamepadConnected(port)) {
      return 0.0f;
    }

    int joystickId = GLFW.GLFW_JOYSTICK_1 + port;
    GLFWGamepadState state = GLFWGamepadState.malloc();
    try {
      if (GLFW.glfwGetGamepadState(joystickId, state)) {
        return state.axes(axis);
      }
      return 0.0f;
    } finally {
      state.free();
    }
  }

  public boolean isButtonPressed(int port, int button) {
    if (!isGamepadConnected(port)) {
      return false;
    }

    int joystickId = GLFW.GLFW_JOYSTICK_1 + port;
    GLFWGamepadState state = GLFWGamepadState.malloc();
    try {
      if (GLFW.glfwGetGamepadState(joystickId, state)) {
        return state.buttons(button) == GLFW.GLFW_PRESS;
      }
      return false;
    } finally {
      state.free();
    }
  }

  /** Current cursor position. */
  public Vector2d getCursorPos() {
    return new Vector2d(mouseX, mouseY);
  }

  /** true if the GameAction was just pressed this frame (keyboard mapping). */
  public boolean isActionJustPressed(GameAction action) {
    int keyCode = getKeyCodeForAction(action);
    if (keyCode < 0) {
      return false;
    }
    return isKeyJustPressed(keyCode);
  }

  /** true if the key was just pressed this frame. */
  public boolean isKeyJustPressed(int keyCode) {
    if (keyCode < 0 || keyCode > GLFW.GLFW_KEY_LAST) {
      return false;
    }
    return keys[keyCode] && !keysLastFrame[keyCode];
  }

  /** true if the mouse button was just pressed this frame. */
  public boolean isMouseButtonJustPressed(int button) {
    if (button < 0 || button > GLFW.GLFW_MOUSE_BUTTON_LAST) {
      return false;
    }
    return mouseButtons[button] && !mouseButtonsLastFrame[button];
  }

  /** Map a GameAction to its default key code for player 0; -1 if none. */
  private int getKeyCodeForAction(GameAction action) {
    return switch (action) {
      case MOVE_UP -> GLFW.GLFW_KEY_W;
      case MOVE_DOWN -> GLFW.GLFW_KEY_S;
      case MOVE_LEFT -> GLFW.GLFW_KEY_A;
      case MOVE_RIGHT -> GLFW.GLFW_KEY_D;
      case ATTACK -> GLFW.GLFW_KEY_SPACE;
      case INTERACT -> GLFW.GLFW_KEY_E;
      case OPEN_MENU -> GLFW.GLFW_KEY_ESCAPE;
    };
  }

  /**
   * Updates the input state tracking for "just pressed" detection.
   * This is called automatically once per frame by the Engine.
   */
  @Override
  public void update() {
    // Copy current states to last frame states
    System.arraycopy(keys, 0, keysLastFrame, 0, keys.length);
    System.arraycopy(mouseButtons, 0, mouseButtonsLastFrame, 0, mouseButtons.length);
    for (int port = 0; port < MAX_GAMEPADS; port++) {
      System.arraycopy(gamepadButtons[port], 0, gamepadButtonsLastFrame[port], 0, gamepadButtons[port].length);
    }

    // Poll gamepad states into current arrays
    for (int port = 0; port < MAX_GAMEPADS; port++) {
      int joystickId = GLFW.GLFW_JOYSTICK_1 + port;
      if (!GLFW.glfwJoystickPresent(joystickId) || !GLFW.glfwJoystickIsGamepad(joystickId)) {
        // Mark all as released if not connected
        Arrays.fill(gamepadButtons[port], (byte) GLFW.GLFW_RELEASE);
        continue;

      }
      GLFWGamepadState state = GLFWGamepadState.malloc();
      try {
        if (GLFW.glfwGetGamepadState(joystickId, state)) {
          for (int b = 0; b <= GLFW.GLFW_GAMEPAD_BUTTON_LAST; b++) {
            gamepadButtons[port][b] = state.buttons(b);
          }
        } else {
          Arrays.fill(gamepadButtons[port], (byte) GLFW.GLFW_RELEASE);
        }
      } finally {
        state.free();
      }
    }
  }
}
