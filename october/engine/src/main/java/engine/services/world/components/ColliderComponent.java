package engine.services.world.components;

import engine.services.world.IComponent;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Introspected
@Getter
@AllArgsConstructor
public final class ColliderComponent implements IComponent {

  /**
   * A marker interface for defining collider types.
   * This allows the game to create its own enum of types that implement this interface,
   * decoupling the engine from game-specific concepts.
   */
  public interface ColliderType {
  }

  private final ColliderType type;
  private final int width;
  private final int height;
  private final int offsetX;
  private final int offsetY;

}
