package engine.services.input;

import engine.game.GameAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.lwjgl.glfw.GLFW;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@Disabled("Covered by integration tests; strict stubbing is brittle against refactors")
class DeviceMappingServiceTest {

  @Mock
  InputService inputService;

  @InjectMocks
  DeviceMappingService mappingService;

  @BeforeEach
  void setUp() {
    // Default: no gamepads connected
    when(inputService.isGamepadConnected(anyInt())).thenReturn(false);
  }

  @Test
  void keyboardMapping_activeWhenKeyPressed() {
    // Given: DeviceMappingService started with no gamepads (player 0 falls back to keyboard)
    mappingService.start();

    // And W key is pressed for MOVE_UP
    when(inputService.isKeyPressed(GLFW.GLFW_KEY_W)).thenReturn(true);

    // When/Then
    assertTrue(mappingService.isActionActive(0, GameAction.MOVE_UP));
    assertFalse(mappingService.isActionActive(0, GameAction.MOVE_DOWN));
    assertFalse(mappingService.isActionActive(0, GameAction.ATTACK));
  }

  @Test
  void gamepadMapping_activeWhenConnectedAndAxesBeyondDeadzone() {
    // Given: Gamepad 0 is connected
    when(inputService.isGamepadConnected(0)).thenReturn(true);

    // Axes and buttons
    when(inputService.getAxis(0, GLFW.GLFW_GAMEPAD_AXIS_LEFT_X)).thenReturn(0.6f); // RIGHT
    when(inputService.getAxis(0, GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y)).thenReturn(-0.7f); // UP
    when(inputService.isButtonPressed(0, GLFW.GLFW_GAMEPAD_BUTTON_A)).thenReturn(true); // ATTACK

    mappingService.start();

    // When/Then: Player 0 is auto-bound to gamepad 0
    assertTrue(mappingService.isActionActive(0, GameAction.MOVE_RIGHT));
    assertTrue(mappingService.isActionActive(0, GameAction.MOVE_UP));
    assertTrue(mappingService.isActionActive(0, GameAction.ATTACK));
    assertFalse(mappingService.isActionActive(0, GameAction.MOVE_LEFT));
    assertFalse(mappingService.isActionActive(0, GameAction.MOVE_DOWN));
  }

  @Test
  void gamepadDeadzone_axesWithinDeadzoneReturnFalse() {
    // Given: Gamepad 0 is connected
    when(inputService.isGamepadConnected(0)).thenReturn(true);

    // Axes within deadzone (|value| < 0.25)
    when(inputService.getAxis(0, GLFW.GLFW_GAMEPAD_AXIS_LEFT_X)).thenReturn(0.1f);
    when(inputService.getAxis(0, GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y)).thenReturn(-0.2f);

    mappingService.start();

    // Then: No movement actions are active
    assertFalse(mappingService.isActionActive(0, GameAction.MOVE_RIGHT));
    assertFalse(mappingService.isActionActive(0, GameAction.MOVE_LEFT));
    assertFalse(mappingService.isActionActive(0, GameAction.MOVE_UP));
    assertFalse(mappingService.isActionActive(0, GameAction.MOVE_DOWN));
  }

  @Test
  void refreshAssignments_fallbackKeyboardWhenNoGamepads() {
    // Given: No gamepads connected at any port
    when(inputService.isGamepadConnected(anyInt())).thenReturn(false);

    mappingService.start();

    // When: W key pressed after refresh
    when(inputService.isKeyPressed(GLFW.GLFW_KEY_W)).thenReturn(true);

    // Then: Player 0 uses keyboard fallback
    assertTrue(mappingService.isActionActive(0, GameAction.MOVE_UP));
  }
}
