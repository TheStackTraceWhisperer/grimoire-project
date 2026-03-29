package engine.services.world.systems;


import engine.services.time.SystemTimeService;
import engine.services.world.ISystem;
import engine.services.world.World;
import engine.game.EnemyComponent;
import engine.services.world.components.TransformComponent;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

/**
 * A simple AI system that makes enemies move back and forth horizontally.
 */
@Prototype
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class EnemyAISystem implements ISystem {

  private final SystemTimeService timeService;
  private final float travelDistance = 3.0f; // How far from the center they patrol

  @Override
  public void update(World world, float deltaTime) {
    applyPatrol(world);
  }

  private void applyPatrol(World world) {
    // Query broadly by TransformComponent, then filter for actual enemies.
    var entities = world.getEntitiesWith(TransformComponent.class);

    for (int entityId : entities) {
      if (!world.hasComponent(entityId, EnemyComponent.class)) {
        continue;
      }
      TransformComponent transform = world.getComponent(entityId, TransformComponent.class);

      // This is a simple sine wave patrol. The enemy's X position will oscillate
      // between -travelDistance and +travelDistance based on the total game time.
      // This creates a smooth back-and-forth movement.
      float horizontalPosition = (float) Math.sin(timeService.getTotalTimeSeconds()) * travelDistance;

      // We must use the vector's methods to modify it, not direct field access.
      transform.position.set(horizontalPosition, transform.position.y(), transform.position.z());
    }
  }
}
