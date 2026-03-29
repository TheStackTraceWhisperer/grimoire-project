package application;

import engine.services.input.InputService;
import engine.services.scene.SceneService;
import engine.services.state.ApplicationState;
import engine.services.state.ApplicationStateService;
import engine.services.world.WorldService;
import engine.services.world.ISystem;
import engine.services.world.systems.UISystem;
import engine.services.world.systems.SequenceSystem;
import engine.services.zone.ZoneService;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import application.ui.TimerOverlaySystem;
import application.ui.TimerOverlayProvider;

import java.util.Collection;
import java.util.List;

@Prototype
@Named("initial")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MainMenuState implements ApplicationState {

  private final SceneService sceneService;
  private final WorldService worldService;
  private final ApplicationStateService applicationStateService;
  private final InputService inputService;
  private final BeanProvider<PlayingState> playingStateProvider;
  private final TimerOverlayProvider timerOverlayProvider;
  private final ZoneService zoneService;

  @Override
  public void onEnter() {
    sceneService.load("/scenes/main_menu.json");
    zoneService.loadZone("main_menu_zone");

    // Configure overlay system instance now that WorldService has enabled it
    TimerOverlaySystem overlay = worldService.getSystem(TimerOverlaySystem.class);
    // TODO: Progress tracking needs to be updated to use components
    timerOverlayProvider.configureMainMenu(overlay, () -> 0.0f);
  }

  @Override
  public void onResume() {
    // Reload the main menu scene and zone
    sceneService.load("/scenes/main_menu.json");
    zoneService.loadZone("main_menu_zone");

    // Reconfigure overlay in case it was recreated
    TimerOverlaySystem overlay = worldService.getSystem(TimerOverlaySystem.class);
    timerOverlayProvider.configureMainMenu(overlay, () -> 0.0f);
  }

  @Override
  public void onSuspend() {
    // No manual system management needed; ApplicationStateService handles enable/disable.
  }

  @Override
  public void onExit() {
    // No manual cleanup needed - systems and entities managed by framework
  }

  @Override
  public Collection<Class<? extends ISystem>> systems() {
    return List.of(UISystem.class, TimerOverlaySystem.class, SequenceSystem.class);
  }

  @EventListener
  public void onStartGame(String event) {
    if ("START_NEW_GAME".equals(event)) {
      applicationStateService.changeState(playingStateProvider::get);
    }
  }

  @Override
  public void onUpdate(float deltaTime) {
    // Input handling moved to a dedicated system
    // TODO: Implement InputResetSystem to handle resetting idle timer on input
  }
}
