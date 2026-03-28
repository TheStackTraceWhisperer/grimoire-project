package engine.services.rendering;

import engine.IService;
import engine.services.resources.AssetCacheService;
import engine.services.window.WindowService;
import engine.services.world.components.UITransformComponent;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class UIRendererService implements IService {

  private final AssetCacheService assetCacheService;
  private final WindowService windowService;

  private UIRenderer renderer;

  @Override
  public int executionOrder() {
    return 31;
  }

  @Override
  public void start() {
    this.renderer = new UIRenderer(assetCacheService, windowService);
    this.renderer.start();
  }

  public void begin() {
    renderer.begin();
  }

  public void submit(UITransformComponent transform, String textureHandle) {
    renderer.submit(transform, textureHandle);
  }

  public void submitColored(UITransformComponent transform, String textureHandle, float r, float g, float b, float a) {
    renderer.submitColored(transform, textureHandle, r, g, b, a);
  }

  public void end() {
    renderer.end();
  }
}
