package engine.services.rendering;

import engine.services.rendering.gl.Shader;
import engine.services.resources.AssetCacheService;
import engine.services.window.WindowService;
import engine.services.world.components.UITransformComponent;
import lombok.RequiredArgsConstructor;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

@RequiredArgsConstructor
public class UIRenderer {

  private final AssetCacheService assetCacheService;
  private final WindowService windowService;
  private CameraService uiCameraService;
  private Shader uiShader;

  public void start() {
    this.uiCameraService = new CameraService();
    this.uiShader = assetCacheService.loadShader("ui", "/shaders/ui.vert", "/shaders/ui.frag");
    windowService.setResizeListener(this::resize);
    resize(windowService.getWidth(), windowService.getHeight());
  }

  public void begin() {
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    uiShader.bind();
    uiShader.setUniform("uProjection", uiCameraService.getProjectionMatrix());
    uiShader.setUniform("uView", new Matrix4f().identity());
  }

  public void submit(UITransformComponent transform, String textureHandle) {
    submitColored(transform, textureHandle, 1.0f, 1.0f, 1.0f, 1.0f);
  }

  public void submitColored(UITransformComponent transform, String textureHandle, float r, float g, float b, float a) {
    if (textureHandle == null) return;
    Texture texture = assetCacheService.resolveTextureHandle(textureHandle);
    Matrix4f modelMatrix = calculateModelMatrix(transform);

    texture.bind(0);
    uiShader.setUniform("uTextureSampler", 0);
    uiShader.setUniform("uModel", modelMatrix);
    uiShader.setUniform("uColor", r, g, b, a);

    Mesh quadMesh = assetCacheService.resolveMeshHandle("quad");
    glBindVertexArray(quadMesh.getVaoId());
    glDrawElements(GL_TRIANGLES, quadMesh.getVertexCount(), GL_UNSIGNED_INT, 0);
    glBindVertexArray(0);
  }

  public void end() {
    uiShader.unbind();
    glDisable(GL_BLEND);
  }

  public void resize(int width, int height) {
    if (uiCameraService != null) {
      uiCameraService.getProjectionMatrix().identity().ortho(0.0f, width, 0.0f, height, -1.0f, 1.0f);
    }
  }

  private Matrix4f calculateModelMatrix(UITransformComponent transform) {
    float[] bounds = transform.screenBounds;
    float width = bounds[2] - bounds[0];
    float height = bounds[3] - bounds[1];
    float posX = bounds[0] + width / 2.0f;
    float posY = bounds[1] + height / 2.0f;
    return new Matrix4f().translate(posX, posY, transform.offset.z).scale(width, height, 1.0f);
  }
}
