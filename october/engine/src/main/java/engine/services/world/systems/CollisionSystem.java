package engine.services.world.systems;


import engine.game.GameColliderType;
import engine.services.world.ISystem;
import engine.services.world.World;
import engine.services.world.components.ColliderComponent;
import engine.services.world.components.TransformComponent;
import io.micronaut.context.annotation.Prototype;

import java.util.ArrayList;
import java.util.List;

@Prototype
public class CollisionSystem implements ISystem {

  @Override
  public void update(World world, float delta) {
    List<Integer> entities = new ArrayList<>(world.getEntitiesWith(TransformComponent.class, ColliderComponent.class));

    for (int i = 0; i < entities.size(); i++) {
      for (int j = i + 1; j < entities.size(); j++) {
        int entityA = entities.get(i);
        int entityB = entities.get(j);

        TransformComponent transformA = world.getComponent(entityA, TransformComponent.class);
        ColliderComponent colliderA = world.getComponent(entityA, ColliderComponent.class);

        TransformComponent transformB = world.getComponent(entityB, TransformComponent.class);
        ColliderComponent colliderB = world.getComponent(entityB, ColliderComponent.class);

        if (checkCollision(transformA, colliderA, transformB, colliderB)) {
          handleCollision(transformA, colliderA, transformB, colliderB);
        }
      }
    }
  }

  private void handleCollision(
      TransformComponent transformA,
      ColliderComponent colliderA,
      TransformComponent transformB,
      ColliderComponent colliderB) {
    ColliderComponent.ColliderType typeA = colliderA.getType();
    ColliderComponent.ColliderType typeB = colliderB.getType();

    // --- Movable vs. Wall Collision ---
    // If a movable entity (Player or Enemy) hits a wall, revert its position.
    if ((typeA == GameColliderType.PLAYER || typeA == GameColliderType.ENEMY)
        && typeB == GameColliderType.WALL) {
      transformA.revertPosition();
    } else if ((typeB == GameColliderType.PLAYER || typeB == GameColliderType.ENEMY)
        && typeA == GameColliderType.WALL) {
      transformB.revertPosition();
    }

    // Future collision types (e.g., Player vs. Enemy) can be added here.
  }

  private boolean checkCollision(
      TransformComponent transformA,
      ColliderComponent colliderA,
      TransformComponent transformB,
      ColliderComponent colliderB) {
    // Use floating-point math to avoid truncation errors at fractional positions
    float x1 = transformA.position.x + colliderA.getOffsetX();
    float y1 = transformA.position.y + colliderA.getOffsetY();
    float w1 = colliderA.getWidth();
    float h1 = colliderA.getHeight();

    float x2 = transformB.position.x + colliderB.getOffsetX();
    float y2 = transformB.position.y + colliderB.getOffsetY();
    float w2 = colliderB.getWidth();
    float h2 = colliderB.getHeight();

    // AABB collision check (treats touching edges as non-colliding)
    return (x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2);
  }
}
