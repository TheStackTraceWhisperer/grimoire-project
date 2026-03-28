package engine.services.input;

import engine.EngineTestHarness;
import engine.game.GameAction;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import io.micronaut.test.annotation.MockBean;
import org.lwjgl.glfw.GLFW;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@MicronautTest
public class DeviceMappingServiceIT extends EngineTestHarness {

  @Inject
  DeviceMappingService mappingService;

  @Inject
  InputService inputService; // This will be the mocked bean

  @MockBean(InputService.class)
  InputService inputService() {
    return Mockito.mock(InputService.class);
  }

  @Test
  void keyboardFallback_player0() {
    // No gamepads connected anywhere
    when(inputService.isGamepadConnected(anyInt())).thenReturn(false);

    mappingService.start();

    // W pressed maps to MOVE_UP for player 0
    when(inputService.isKeyPressed(GLFW.GLFW_KEY_W)).thenReturn(true);
    assertTrue(mappingService.isActionActive(0, GameAction.MOVE_UP));
    assertFalse(mappingService.isActionActive(0, GameAction.MOVE_DOWN));
  }

  @Test
  void gamepad0_assignedToPlayer0() {
    // Gamepad 0 connected
    when(inputService.isGamepadConnected(0)).thenReturn(true);

    // Axes beyond deadzone for right and up, and attack button pressed
    when(inputService.getAxis(0, GLFW.GLFW_GAMEPAD_AXIS_LEFT_X)).thenReturn(0.7f);
    when(inputService.getAxis(0, GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y)).thenReturn(-0.8f);
    when(inputService.isButtonPressed(0, GLFW.GLFW_GAMEPAD_BUTTON_A)).thenReturn(true);

    mappingService.start();

    assertTrue(mappingService.isActionActive(0, GameAction.MOVE_RIGHT));
    assertTrue(mappingService.isActionActive(0, GameAction.MOVE_UP));
    assertTrue(mappingService.isActionActive(0, GameAction.ATTACK));
    assertFalse(mappingService.isActionActive(0, GameAction.MOVE_LEFT));
    assertFalse(mappingService.isActionActive(0, GameAction.MOVE_DOWN));
  }

  @Test
  void deadzone_preventsSmallAxis() {
    when(inputService.isGamepadConnected(0)).thenReturn(true);
    when(inputService.getAxis(0, GLFW.GLFW_GAMEPAD_AXIS_LEFT_X)).thenReturn(0.1f);
    when(inputService.getAxis(0, GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y)).thenReturn(0.2f);

    mappingService.start();

    assertFalse(mappingService.isActionActive(0, GameAction.MOVE_RIGHT));
    assertFalse(mappingService.isActionActive(0, GameAction.MOVE_DOWN));
  }
}
