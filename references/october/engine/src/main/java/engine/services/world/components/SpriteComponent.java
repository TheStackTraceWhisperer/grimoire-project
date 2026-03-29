package engine.services.world.components;

import engine.services.world.IComponent;
import io.micronaut.core.annotation.Introspected;
import org.joml.Vector4f;

import java.util.Objects;

/**
 * A component that defines the visual appearance of a 2D sprite.
 * It holds a handle to the texture to be rendered and an optional color tint.
 * This is a record, which is an immutable data carrier.
 */
@Introspected
public record SpriteComponent(String textureHandle, Vector4f color) implements IComponent {

  /**
   * A compact constructor for the record.
   * This is automatically called by the canonical constructor.
   * It ensures that if the color is not provided during deserialization (i.e., it's null),
   * it defaults to white.
   */
  public SpriteComponent {
    Objects.requireNonNull(textureHandle, "textureHandle must not be null");
    if (color == null) {
      color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
  }

  public SpriteComponent(String textureHandle) {
    this(textureHandle, null);
  }
}
