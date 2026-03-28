package engine.services.world;

import engine.services.world.components.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComponentRegistryTest {

  @Test
  void constructor_shouldDiscoverAllIntrospectedComponents() {
    // When
    ComponentRegistry registry = new ComponentRegistry(null);

    // Then - should have discovered all @Introspected component classes
    assertThat(registry.size()).isGreaterThan(0);
    assertThat(registry.getComponentNames()).contains(
      "AudioSourceComponent",
      "ColliderComponent",
      "ControllableComponent",
      "EnemyComponent",
      "HealthComponent",
      "MeshComponent",
      "MovementStatsComponent",
      "MusicComponent",
      "PlayerComponent",
      "SoundEffectComponent",
      "SpriteComponent",
      "TransformComponent",
      "UIButtonComponent",
      "UIImageComponent",
      "UITransformComponent"
    );
  }

  @Test
  void getComponentClass_shouldReturnCorrectClass() {
    // Given
    ComponentRegistry registry = new ComponentRegistry(null);

    // When/Then
    assertThat(registry.getComponentClass("TransformComponent"))
      .isEqualTo(TransformComponent.class);
    assertThat(registry.getComponentClass("SpriteComponent"))
      .isEqualTo(SpriteComponent.class);
    assertThat(registry.getComponentClass("HealthComponent"))
      .isEqualTo(HealthComponent.class);
  }

  @Test
  void getComponentClass_shouldReturnNullForUnknownComponent() {
    // Given
    ComponentRegistry registry = new ComponentRegistry(null);

    // When/Then
    assertThat(registry.getComponentClass("NonExistentComponent")).isNull();
  }

  @Test
  void getComponentNames_shouldReturnAllRegisteredComponentNames() {
    // Given
    ComponentRegistry registry = new ComponentRegistry(null);

    // When
    var componentNames = registry.getComponentNames();

    // Then
    assertThat(componentNames).isNotEmpty();
    assertThat(componentNames).contains("TransformComponent", "SpriteComponent");
  }

  @Test
  void size_shouldReturnNumberOfRegisteredComponents() {
    // Given
    ComponentRegistry registry = new ComponentRegistry(null);

    // When
    int size = registry.size();

    // Then
    assertThat(size).isGreaterThan(10); // We have at least 15 components
  }
}
