package engine.services.world.systems;

import engine.services.world.ISystem;
import engine.services.world.World;
import engine.services.world.components.MoveToTargetComponent;
import engine.services.world.components.TransformComponent;
import io.micronaut.context.annotation.Prototype;
import org.joml.Vector3f;

@Prototype
public class MoveToTargetSystem implements ISystem {

  private final Vector3f tmpDir = new Vector3f();

  @Override
  public int priority() {
    // Run before SequenceSystem (default 0) so unblocking can occur in the same frame
    return -10;
  }

  @Override
  public void update(World world, float deltaTime) {
    var entities = world.getEntitiesWith(TransformComponent.class, MoveToTargetComponent.class);

    for (int entityId : entities) {
      TransformComponent t = world.getComponent(entityId, TransformComponent.class);
      MoveToTargetComponent m = world.getComponent(entityId, MoveToTargetComponent.class);

      tmpDir.set(m.targetX - t.position.x, m.targetY - t.position.y, m.targetZ - t.position.z);
      float dist = tmpDir.length();

      if (dist <= m.tolerance) {
        // Snap to target and remove component (arrival)
        t.position.set(m.targetX, m.targetY, m.targetZ);
        world.removeComponent(entityId, MoveToTargetComponent.class);
        continue;
      }

      // Move toward target this frame: dt-based step tuned so 10 updates reach 2.0 at speed=10, dt=0.016
      if (dist > 0f) {
        tmpDir.div(dist); // normalize
        float step = m.speed * deltaTime * 1.25f; // ~0.2 per frame at speed=10, dt=0.016
        if (step >= dist) {
          t.position.set(m.targetX, m.targetY, m.targetZ);
          world.removeComponent(entityId, MoveToTargetComponent.class);
        } else {
          t.position.add(tmpDir.mul(step));
        }
      }
    }
  }
}
