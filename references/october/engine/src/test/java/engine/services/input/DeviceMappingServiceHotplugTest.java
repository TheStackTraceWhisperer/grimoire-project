package engine.services.input;

import engine.game.GameAction;
import org.junit.jupiter.api.BeforeEach;
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
@MockitoSettings(strictness = Strictness.LENIENT)
class DeviceMappingServiceHotplugTest {

  @Mock
  InputService inputService;

  @InjectMocks
  DeviceMappingService mappingService;

  @BeforeEach
  void baseline() {
    // Default: nothing connected
    when(inputService.isGamepadConnected(anyInt())).thenReturn(false);
  }

  @Test
  void hotplug_refresh_assigns_gamepad_after_interval() {
    mappingService.start();

    // Now gamepad 0 connects and reports inputs
    when(inputService.isGamepadConnected(0)).thenReturn(true);
    when(inputService.getAxis(0, GLFW.GLFW_GAMEPAD_AXIS_LEFT_X)).thenReturn(0.8f);
    when(inputService.getAxis(0, GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y)).thenReturn(0.0f);

    // Simulate 1.0s passing to trigger refresh
    mappingService.update(1.0f);

    assertTrue(mappingService.isActionActive(0, GameAction.MOVE_RIGHT));
  }

  @Test
  void multiple_gamepads_assign_players_in_order() {
    when(inputService.isGamepadConnected(0)).thenReturn(true);
    when(inputService.isGamepadConnected(1)).thenReturn(true);

    mappingService.start();

    // Provide input for gamepad 1 -> affects player 1
    when(inputService.getAxis(1, GLFW.GLFW_GAMEPAD_AXIS_LEFT_X)).thenReturn(-0.9f);
    when(inputService.getAxis(1, GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y)).thenReturn(0.9f);

    assertTrue(mappingService.isActionActive(1, GameAction.MOVE_LEFT));
    assertTrue(mappingService.isActionActive(1, GameAction.MOVE_DOWN));
  }

  @Test
  void bind_out_of_range_is_ignored_and_fallback_keyboard_for_player0() {
    mappingService.start();
    // Out-of-range binds should not throw or corrupt state
    mappingService.bindPlayerToGamepad(-1, 0);
    mappingService.bindPlayerToGamepad(0, 8);

    // With no gamepads, player 0 should still use keyboard fallback
    when(inputService.isKeyPressed(GLFW.GLFW_KEY_W)).thenReturn(true);
    assertTrue(mappingService.isActionActive(0, GameAction.MOVE_UP));
  }

  @Test
  void getMaxSupportedPlayers_and_unmapped_actions() {
    assertEquals(8, mappingService.getMaxSupportedPlayers());

    mappingService.start();
    // OPEN_MENU not mapped for keyboard in DeviceMappingService; should be false
    assertFalse(mappingService.isActionActive(0, GameAction.OPEN_MENU));
  }
}
