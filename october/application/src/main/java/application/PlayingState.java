package application;

import engine.services.rendering.CameraService;
import engine.services.scene.SceneService;
import engine.services.state.ApplicationState;
import engine.services.window.WindowService;
import engine.services.world.WorldService;
import engine.services.world.ISystem;
import engine.services.world.systems.*;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;

@Prototype
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlayingState implements ApplicationState {

  private final SceneService sceneService;
  private final WorldService worldService;
  private final CameraService cameraService;
  private final WindowService windowService;

  @Override
  public void onEnter() {
    // --- Set up the game world ---
    sceneService.load("/scenes/playing-scene.json");

    // --- Configure the main game camera ---
    cameraService.setPosition(new Vector3f(0.0f, 0.0f, 5.0f));
    cameraService.resize(windowService.getWidth(), windowService.getHeight());
    windowService.setResizeListener(cameraService::resize);
  }

  @Override
  public void onUpdate(float deltaTime) {
    // All logic is handled by the systems
  }

  @Override
  public void onExit() {
    // No manual clearing; ApplicationStateService will disable systems
  }

  @Override
  public Collection<Class<? extends ISystem>> systems() {
    return List.of(
      PlayerInputSystem.class,
      MovementSystem.class,
      EnemyAISystem.class,
      CollisionSystem.class,
      TriggerSystem.class,
      SequenceSystem.class,
      MoveToTargetSystem.class,
      AudioSystem.class,
      RenderSystem.class,
      UISystem.class,
      FadeOverlaySystem.class
    );
  }
}
