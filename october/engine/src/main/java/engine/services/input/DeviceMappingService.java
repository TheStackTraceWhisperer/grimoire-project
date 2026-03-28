package engine.services.input;

import engine.IService;
import engine.game.GameAction;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/** Maps players to keyboard/gamepad inputs with hotplug support. */
@Singleton
@RequiredArgsConstructor
public class DeviceMappingService implements IService {

  private static final int MAX_PLAYERS = 8;
  private static final float DEADZONE = 0.25f;
  private static final float HOTPLUG_REFRESH_INTERVAL = 1.0f; // seconds

  private final InputService inputService;

  // Device bindings: player -> device type
  private final Map<Integer, DeviceBinding> playerBindings = new HashMap<>();

  // Keyboard mappings
  private final Map<Integer, Map<GameAction, Integer>> playerKeyMappings = new HashMap<>();

  private float hotplugTimer = 0.0f;

  @Override
  public int executionOrder() {
    return 16;
  }

  @Override
  public void start() {
    loadDefaultMappings();
    refreshAssignments();
  }

  @Override
  public void update(float dt) {
    // Periodically refresh device assignments to handle hotplug events
    hotplugTimer += dt;
    if (hotplugTimer >= HOTPLUG_REFRESH_INTERVAL) {
      hotplugTimer = 0.0f;
      refreshAssignments();
    }
  }

  private void loadDefaultMappings() {
    // Player 0 keyboard controls: WASD + Space
    Map<GameAction, Integer> player0KeyMap = new HashMap<>();
    player0KeyMap.put(GameAction.MOVE_UP, GLFW.GLFW_KEY_W);
    player0KeyMap.put(GameAction.MOVE_DOWN, GLFW.GLFW_KEY_S);
    player0KeyMap.put(GameAction.MOVE_LEFT, GLFW.GLFW_KEY_A);
    player0KeyMap.put(GameAction.MOVE_RIGHT, GLFW.GLFW_KEY_D);
    player0KeyMap.put(GameAction.ATTACK, GLFW.GLFW_KEY_SPACE);
    playerKeyMappings.put(0, player0KeyMap);
  }

  public boolean isActionActive(int playerId, GameAction action) {
    DeviceBinding binding = playerBindings.get(playerId);
    if (binding == null) {
      return false;
    }

    switch (binding.type) {
      case KEYBOARD:
        return isKeyboardActionActive(playerId, action);
      case GAMEPAD:
        return isGamepadActionActive(binding.deviceIndex, action);
      default:
        return false;
    }
  }

  private boolean isKeyboardActionActive(int playerId, GameAction action) {
    Map<GameAction, Integer> mappings = playerKeyMappings.get(playerId);
    if (mappings == null) {
      return false;
    }

    Integer keyCode = mappings.get(action);
    if (keyCode == null) {
      return false;
    }

    return inputService.isKeyPressed(keyCode);
  }

  private boolean isGamepadActionActive(int gamepadIndex, GameAction action) {
    if (!inputService.isGamepadConnected(gamepadIndex)) {
      return false;
    }

    switch (action) {
      case MOVE_UP:
        return inputService.getAxis(gamepadIndex, GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y) < -DEADZONE;
      case MOVE_DOWN:
        return inputService.getAxis(gamepadIndex, GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y) > DEADZONE;
      case MOVE_LEFT:
        return inputService.getAxis(gamepadIndex, GLFW.GLFW_GAMEPAD_AXIS_LEFT_X) < -DEADZONE;
      case MOVE_RIGHT:
        return inputService.getAxis(gamepadIndex, GLFW.GLFW_GAMEPAD_AXIS_LEFT_X) > DEADZONE;
      case ATTACK:
        return inputService.isButtonPressed(gamepadIndex, GLFW.GLFW_GAMEPAD_BUTTON_A);
      default:
        return false;
    }
  }

  /** Bind a player to keyboard input. */
  public void bindPlayerToKeyboard(int playerId) {
    if (playerId >= 0 && playerId < MAX_PLAYERS) {
      playerBindings.put(playerId, new DeviceBinding(DeviceType.KEYBOARD, -1));
    }
  }

  /** Bind a player to a specific gamepad. */
  public void bindPlayerToGamepad(int playerId, int gamepadIndex) {
    if (playerId >= 0 && playerId < MAX_PLAYERS && gamepadIndex >= 0 && gamepadIndex < MAX_PLAYERS) {
      playerBindings.put(playerId, new DeviceBinding(DeviceType.GAMEPAD, gamepadIndex));
    }
  }

  /** Maximum supported players. */
  public int getMaxSupportedPlayers() {
    return MAX_PLAYERS;
  }

  /** Refresh device assignments and ensure player 0 is bound. */
  public void refreshAssignments() {
    playerBindings.clear();

    // Auto-assign connected gamepads to players
    for (int gamepadIndex = 0; gamepadIndex < MAX_PLAYERS; gamepadIndex++) {
      if (inputService.isGamepadConnected(gamepadIndex)) {
        playerBindings.put(gamepadIndex, new DeviceBinding(DeviceType.GAMEPAD, gamepadIndex));
      }
    }

    // Ensure player 0 always has a binding
    if (!playerBindings.containsKey(0)) {
      bindPlayerToKeyboard(0);
    }
  }

  private enum DeviceType {
    KEYBOARD,
    GAMEPAD
  }

  private static class DeviceBinding {
    final DeviceType type;
    final int deviceIndex; // -1 for keyboard, 0-7 for gamepad

    DeviceBinding(DeviceType type, int deviceIndex) {
      this.type = type;
      this.deviceIndex = deviceIndex;
    }
  }
}
