package application;

import engine.services.state.ApplicationState;
import engine.services.state.ApplicationStateService;
import engine.services.input.InputService;
import engine.services.scene.SceneService;
import engine.services.world.WorldService;
import engine.services.world.ISystem;
import engine.services.world.systems.TriggerSystem;
import engine.services.world.systems.SequenceSystem;
import engine.services.world.systems.MovementSystem;
import engine.services.world.systems.AudioSystem;
import engine.services.world.systems.MoveToTargetSystem;
import engine.services.world.systems.FadeOverlaySystem;
import engine.services.world.systems.RenderSystem;
import engine.services.world.systems.UISystem;
import engine.services.zone.ZoneService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.micronaut.runtime.event.annotation.EventListener;
import application.ui.TimerOverlaySystem;
import application.ui.TimerOverlayProvider;

import java.util.Collection;
import java.util.List;

/** Intro cutscene that ties together zone, tilemap, and sequences. */
@Singleton
@Slf4j
@Named("introCutscene")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class IntroCutsceneState implements ApplicationState {

  private final ApplicationStateService applicationStateService;
  private final InputService inputService;
  private final WorldService worldService;
  private final SceneService sceneService;
  private final ZoneService zoneService;
  private final TimerOverlayProvider timerOverlayProvider;

  @Override
  public void onEnter() {
    log.debug("Entering IntroCutsceneState");

    // Clear any existing entities (e.g., main menu UI) so the cutscene fully takes over
    sceneService.load("/scenes/cutscene-blank.json");

    // Load the intro cutscene zone (publishes ZoneLoadedEvent)
    zoneService.loadZone("intro_cutscene_zone");

    // Configure progress overlay; WorldService will enable the system using classes
    TimerOverlaySystem overlay = worldService.getSystem(TimerOverlaySystem.class);
    // TODO: Progress tracking needs to be updated to use components
    timerOverlayProvider.configureIntroCutscene(overlay, () -> 0.0f);

    log.debug("IntroCutsceneState systems registered and zone loading initiated");
  }

  @Override
  public void onExit() {
    log.debug("Exiting IntroCutsceneState");
    // No manual cleanup needed - systems and entities managed by framework
  }

  @Override
  public Collection<Class<? extends ISystem>> systems() {
    return List.of(
      TriggerSystem.class,
      SequenceSystem.class,
      MovementSystem.class,
      MoveToTargetSystem.class,
      AudioSystem.class,
      RenderSystem.class,
      UISystem.class,
      FadeOverlaySystem.class,
      TimerOverlaySystem.class
    );
  }

  @Override
  public void onUpdate(float deltaTime) {
    handleInput();
  }

  public void handleInput() {
    // Skip on any keyboard key, mouse button, or gamepad button just-pressed; ignore cursor movement
    if (inputService.isAnyKeyJustPressed() || inputService.isAnyMouseButtonJustPressed() || inputService.isAnyGamepadButtonJustPressed()) {
      applicationStateService.popState();
    }
  }
}
