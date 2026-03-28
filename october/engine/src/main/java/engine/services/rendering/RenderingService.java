package engine.services.rendering;

import engine.IService;
import engine.services.resources.AssetCacheService;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.joml.Matrix4f;

@Singleton
@RequiredArgsConstructor
public class RenderingService implements IService {

  private final AssetCacheService assetCacheService;

  private Renderer renderer;

  @Override
  public int executionOrder() {
    return 30;
  }

  @Override
  public void start() {
    this.renderer = new Renderer(assetCacheService);
    this.renderer.start();
  }

  public void beginScene(CameraService cameraService) {
    renderer.beginScene(cameraService);
  }

  public void submit(Mesh mesh, Texture texture, Matrix4f transform) {
    renderer.submit(mesh, texture, transform);
  }

  public void endScene() {
    renderer.endScene();
  }
}
