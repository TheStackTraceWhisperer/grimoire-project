package engine.services.world.systems;

import engine.services.rendering.CameraService;
import engine.services.rendering.Mesh;
import engine.services.rendering.RenderingService;
import engine.services.rendering.Texture;
import engine.services.resources.AssetCacheService;
import engine.services.world.ISystem;
import engine.services.world.World;
import engine.services.world.components.SpriteComponent;
import engine.services.world.components.TransformComponent;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

/**
 * The system responsible for rendering all sprite entities.
 * <p>
 * This system acts as the bridge between the ECS and the rendering engine.
 * It queries the world for entities with a Transform and a Sprite, resolves their
 * texture and mesh resources, and submits them to the Renderer to be drawn.
 */
@Prototype
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RenderSystem implements ISystem {

  private final RenderingService renderingService;
  private final AssetCacheService resourceManager;
  private final CameraService cameraService;

  @Override
  public void update(World world, float deltaTime) {
    renderingService.beginScene(cameraService);

    // Get all entities that have the components required for sprite rendering
    var renderableEntities = world.getEntitiesWith(TransformComponent.class, SpriteComponent.class);

    // For a 2D sprite game, all sprites will use the same underlying quad mesh.
    // We can resolve this once outside the loop for efficiency.
    Mesh quadMesh = resourceManager.resolveMeshHandle("quad");

    for (int entityId : renderableEntities) {
      TransformComponent transform = world.getComponent(entityId, TransformComponent.class);
      SpriteComponent sprite = world.getComponent(entityId, SpriteComponent.class);

      // Use the handle from the SpriteComponent to get the actual Texture resource
      Texture texture = resourceManager.resolveTextureHandle(sprite.textureHandle());

      // Submit the quad mesh, the specific texture, and the transform to the renderer.
      renderingService.submit(quadMesh, texture, transform.getTransformMatrix());
    }

    renderingService.endScene();
  }
}
