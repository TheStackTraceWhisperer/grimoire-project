package engine.services.scene;

import engine.EngineTestHarness;
import engine.services.resources.AssetCacheService;
import engine.services.world.WorldService;
import engine.services.world.components.SpriteComponent;
import engine.services.world.components.TransformComponent;
import jakarta.inject.Inject;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SceneServiceIT extends EngineTestHarness {

    @Inject
    private SceneService sceneService;
    @Inject
    private WorldService worldService;
    @Inject
    private AssetCacheService assetCacheService;

    @Test
    void testLoadScene() {
        // Given a valid scene file path
        String scenePath = "/scenes/test-scene.json";

        // When we load the scene
        sceneService.load(scenePath);

        // Then the assets from the manifest should be loaded
        assertNotNull(assetCacheService.resolveTextureHandle("test-scene-texture"),
                "Texture from scene manifest should be loaded into AssetCacheService.");

        // And the entities from the scene should be created in the world
        var entities = worldService.getEntitiesWith(TransformComponent.class, SpriteComponent.class);
        assertEquals(1, entities.size(), "There should be exactly one entity created from the scene.");

        // And the entity's components should have the correct data
        int entityId = entities.iterator().next();
        TransformComponent transform = worldService.getComponent(entityId, TransformComponent.class);
        SpriteComponent sprite = worldService.getComponent(entityId, SpriteComponent.class);

        assertNotNull(transform, "Entity should have a TransformComponent.");
        assertEquals(new Vector3f(10.0f, 20.0f, 30.0f), transform.position, "Transform position should match scene data.");
        assertEquals(new Vector3f(2.0f, 2.0f, 2.0f), transform.scale, "Transform scale should match scene data.");

        assertNotNull(sprite, "Entity should have a SpriteComponent.");
        assertEquals("test-scene-texture", sprite.textureHandle(), "Sprite texture handle should match scene data.");
    }
}
