package engine.services.rendering;

import engine.services.rendering.gl.Shader;
import engine.services.resources.AssetCacheService;
import lombok.RequiredArgsConstructor;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

@RequiredArgsConstructor
public class Renderer {

  private final AssetCacheService assetCacheService;

  private Shader instancedShader;
  private SpriteBatch spriteBatch;
  private InstancedMesh quadMesh;

  public void start() {
    this.instancedShader = assetCacheService.loadShader(
      "default",
      "/shaders/default.vert",
      "/shaders/default.frag"
    );
    this.spriteBatch = new SpriteBatch();

    float[] vertices = {
      -0.5f, 0.5f, 0.0f,   0.0f, 1.0f,
      0.5f, 0.5f, 0.0f,    1.0f, 1.0f,
      0.5f, -0.5f, 0.0f,   1.0f, 0.0f,
      -0.5f, -0.5f, 0.0f,  0.0f, 0.0f
    };
    int[] indices = { 0, 3, 2, 2, 1, 0 };
    this.quadMesh = new InstancedMesh(vertices, indices);
  }

  public void beginScene(CameraService cameraService) {
    glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    spriteBatch.clear();

    instancedShader.bind();
    instancedShader.setUniform("uProjection", cameraService.getProjectionMatrix());
    instancedShader.setUniform("uView", cameraService.getViewMatrix());
  }

  public void submit(Mesh mesh, Texture texture, Matrix4f transform) {
    spriteBatch.addSprite(texture, new Matrix4f(transform));
  }

  public void endScene() {
    for (Texture texture : spriteBatch.getTextures()) {
      var transforms = spriteBatch.getSpritesForTexture(texture);
      if (!transforms.isEmpty()) {
        texture.bind(0);
        instancedShader.setUniform("uTextureSampler", 0);
        quadMesh.renderInstanced(transforms);
      }
    }
    instancedShader.unbind();
  }
}
