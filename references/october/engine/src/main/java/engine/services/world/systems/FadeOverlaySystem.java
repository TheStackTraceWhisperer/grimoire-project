package engine.services.world.systems;

import engine.services.rendering.FadeService;
import engine.services.rendering.UIRendererService;
import engine.services.window.WindowService;
import engine.services.world.ISystem;
import engine.services.world.World;
import engine.services.world.components.UITransformComponent;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@Prototype
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class FadeOverlaySystem implements ISystem {

  private final FadeService fadeService;
  private final UIRendererService uiRendererService;
  private final WindowService windowService;

  @Override
  public int priority() {
    // Render after UI to ensure it overlays everything
    return 11;
  }

  @Override
  public void update(World world, float deltaTime) {
    if (!fadeService.isFading()) {
      return;
    }

    float progress = fadeService.getProgress();
    String type = fadeService.getFadeType();
    float alpha;
    if ("IN".equalsIgnoreCase(type)) {
      alpha = 1.0f - progress; // fade in from black to clear
    } else {
      // Default to OUT behavior: clear to black
      alpha = progress;
    }

    if (alpha <= 0f) {
      return;
    }

    int width = windowService.getWidth();
    int height = windowService.getHeight();

    UITransformComponent fullscreen = new UITransformComponent();
    fullscreen.relativeSize = false;
    fullscreen.screenBounds[0] = 0f;
    fullscreen.screenBounds[1] = 0f;
    fullscreen.screenBounds[2] = width;
    fullscreen.screenBounds[3] = height;

    uiRendererService.begin();
    // Use the built-in 1x1 white texture and tint it black with alpha
    uiRendererService.submitColored(fullscreen, "white", 0f, 0f, 0f, alpha);
    uiRendererService.end();
  }
}
