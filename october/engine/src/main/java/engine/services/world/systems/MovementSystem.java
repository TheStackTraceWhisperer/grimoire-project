package engine.services.world.systems;

import engine.services.world.ISystem;
import engine.services.world.World;
import engine.services.world.components.ControllableComponent;
import engine.services.world.components.MovementStatsComponent;
import engine.services.world.components.TransformComponent;
import io.micronaut.context.annotation.Prototype;
import org.joml.Vector3f;

/**
 * This system is responsible for moving entities based on their ControllableComponent state.
 * It reads the player's intent and applies it to the entity's TransformComponent.
 */
@Prototype
public class MovementSystem implements ISystem {

  // Re-use a single vector object per frame to avoid creating garbage
  private final Vector3f velocity = new Vector3f();


  @Override
  public void update(World world, float deltaTime) {
    // Get all entities that can be moved by the player
    var entities = world.getEntitiesWith(
      ControllableComponent.class,
      TransformComponent.class,
      MovementStatsComponent.class
    );

    for (int entityId : entities) {
      ControllableComponent control = world.getComponent(entityId, ControllableComponent.class);
      TransformComponent transform = world.getComponent(entityId, TransformComponent.class);
      MovementStatsComponent stats = world.getComponent(entityId, MovementStatsComponent.class);

      // Snapshot the position before we move it. This is crucial for collision response.
      transform.updatePreviousPosition();

      // Reset velocity for this frame
      velocity.zero();

      // Build a direction vector from the player's intent
      if (control.wantsToMoveUp) {
        velocity.y += 1;
      }
      if (control.wantsToMoveDown) {
        velocity.y -= 1;
      }
      if (control.wantsToMoveLeft) {
        velocity.x -= 1;
      }
      if (control.wantsToMoveRight) {
        velocity.x += 1;
      }

      // Normalize the vector if the player is moving diagonally.
      // This is crucial to prevent faster movement on diagonals.
      if (velocity.lengthSquared() > 0) {
        velocity.normalize();
      }

      // Apply speed and delta time to calculate the final movement for this frame
      velocity.mul(stats.speed() * deltaTime);

      // Add the final movement vector to the entity's current position
      transform.position.add(velocity);
    }
  }
}
