package engine.services.world.components;

import engine.services.world.IComponent;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class MoveToTargetComponent implements IComponent {
  public float targetX;
  public float targetY;
  public float targetZ;
  public float speed;      // units per second
  public float tolerance;  // distance threshold to consider arrived

  public MoveToTargetComponent(float targetX, float targetY, float targetZ, float speed, float tolerance) {
    this.targetX = targetX;
    this.targetY = targetY;
    this.targetZ = targetZ;
    this.speed = speed;
    this.tolerance = tolerance;
  }
}
