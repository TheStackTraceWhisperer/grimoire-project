package engine.services.scene;

import engine.services.resources.AssetCacheService;
import engine.services.world.ComponentRegistry;
import engine.services.world.WorldService;
import engine.services.world.components.SpriteComponent;
import engine.services.world.components.TransformComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SceneServiceTest {

  @Mock
  private WorldService worldService;

  @Mock
  private AssetCacheService assetCacheService;

  private ComponentRegistry componentRegistry;
  private SceneService sceneService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    componentRegistry = new ComponentRegistry(null);
    sceneService = new SceneService(worldService, assetCacheService, componentRegistry);
  }

  @Test
  void constructor_shouldInjectComponentRegistry() {
    // Given/When
    SceneService service = new SceneService(worldService, assetCacheService, componentRegistry);

    // Then
    assertThat(service).isNotNull();
  }

  @Test
  void executionOrder_shouldReturn60() {
    // When
    int order = sceneService.executionOrder();

    // Then
    assertThat(order).isEqualTo(60);
  }

  @Test
  void componentRegistry_shouldResolveComponentClasses() {
    // When
    Class<?> transformClass = componentRegistry.getComponentClass("TransformComponent");
    Class<?> spriteClass = componentRegistry.getComponentClass("SpriteComponent");

    // Then
    assertThat(transformClass).isEqualTo(TransformComponent.class);
    assertThat(spriteClass).isEqualTo(SpriteComponent.class);
  }
}
