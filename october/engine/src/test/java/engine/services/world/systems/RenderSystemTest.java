package engine.services.world.systems;

import engine.services.rendering.CameraService;
import engine.services.rendering.Mesh;
import engine.services.rendering.RenderingService;
import engine.services.rendering.Texture;
import engine.services.resources.AssetCacheService;
import engine.services.world.World;
import engine.services.world.components.SpriteComponent;
import engine.services.world.components.TransformComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RenderSystemTest {

    @Mock
    private RenderingService renderingService;
    @Mock
    private AssetCacheService resourceManager;
    @Mock
    private CameraService cameraService;
    @Mock
    private World world;

    @InjectMocks
    private RenderSystem renderSystem;

    @Test
    void update_shouldBeginSceneAndEndScene() {
        // Arrange
        when(world.getEntitiesWith(TransformComponent.class, SpriteComponent.class)).thenReturn(Set.of());

        // Act
        renderSystem.update(world, 0.1f);

        // Assert
        verify(renderingService).beginScene(cameraService);
        verify(renderingService).endScene();
    }

    @Test
    void update_shouldSubmitRenderablesToService() {
        // Arrange
        int entityId = 1;
        var transform = new TransformComponent();
        var sprite = new SpriteComponent("test_texture");
        var mesh = mock(Mesh.class);
        var texture = mock(Texture.class);

        when(world.getEntitiesWith(TransformComponent.class, SpriteComponent.class)).thenReturn(Set.of(entityId));
        when(world.getComponent(entityId, TransformComponent.class)).thenReturn(transform);
        when(world.getComponent(entityId, SpriteComponent.class)).thenReturn(sprite);
        when(resourceManager.resolveMeshHandle("quad")).thenReturn(mesh);
        when(resourceManager.resolveTextureHandle("test_texture")).thenReturn(texture);

        // Act
        renderSystem.update(world, 0.1f);

        // Assert
        verify(renderingService).submit(mesh, texture, transform.getTransformMatrix());
    }
}
