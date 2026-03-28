package engine.services.world.components;

import engine.services.rendering.Direction;
import engine.services.world.IComponent;
import io.micronaut.core.annotation.Introspected;

import java.util.Objects;

/**
 * A component for entities that use animated sprites with directional support.
 * <p>
 * This component extends sprite support to include:
 * <ul>
 *   <li>Sprite sheet reference instead of individual texture</li>
 *   <li>Current animation state</li>
 *   <li>Direction-based animation mapping</li>
 *   <li>Animation playback timing</li>
 * </ul>
 * <p>
 * Unlike the basic SpriteComponent, this component is mutable to support
 * animation state changes during gameplay.
 */
@Introspected
public record AnimatedSpriteComponent(
    String spriteSheetHandle,
    String currentAnimation,
    Direction currentDirection,
    float animationTime,
    boolean playing
) implements IComponent {

  /**
   * Compact constructor with validation.
   */
  public AnimatedSpriteComponent {
    Objects.requireNonNull(spriteSheetHandle, "spriteSheetHandle must not be null");
    Objects.requireNonNull(currentAnimation, "currentAnimation must not be null");
    Objects.requireNonNull(currentDirection, "currentDirection must not be null");
    if (animationTime < 0) {
      throw new IllegalArgumentException("animationTime must be non-negative, got: " + animationTime);
    }
  }

  /**
   * Creates a new AnimatedSpriteComponent with default values.
   *
   * @param spriteSheetHandle The handle to the sprite sheet resource
   * @param initialAnimation The initial animation to play
   */
  public AnimatedSpriteComponent(String spriteSheetHandle, String initialAnimation) {
    this(spriteSheetHandle, initialAnimation, Direction.DOWN, 0.0f, true);
  }

  /**
   * Creates a copy of this component with a new animation.
   *
   * @param newAnimation The new animation name
   * @return A new component with the animation changed
   */
  public AnimatedSpriteComponent withAnimation(String newAnimation) {
    return new AnimatedSpriteComponent(spriteSheetHandle, newAnimation, currentDirection, 0.0f, playing);
  }

  /**
   * Creates a copy of this component with a new direction.
   *
   * @param newDirection The new direction
   * @return A new component with the direction changed
   */
  public AnimatedSpriteComponent withDirection(Direction newDirection) {
    return new AnimatedSpriteComponent(spriteSheetHandle, currentAnimation, newDirection, animationTime, playing);
  }

  /**
   * Creates a copy of this component with updated animation time.
   *
   * @param deltaTime The time to add to the current animation time
   * @return A new component with the time updated
   */
  public AnimatedSpriteComponent withTimeAdvanced(float deltaTime) {
    return new AnimatedSpriteComponent(spriteSheetHandle, currentAnimation, currentDirection,
                                      animationTime + deltaTime, playing);
  }

  /**
   * Creates a copy of this component with the playing state changed.
   *
   * @param isPlaying Whether the animation should be playing
   * @return A new component with the playing state changed
   */
  public AnimatedSpriteComponent withPlaying(boolean isPlaying) {
    return new AnimatedSpriteComponent(spriteSheetHandle, currentAnimation, currentDirection,
                                      animationTime, isPlaying);
  }

  /**
   * Creates a copy of this component with the animation reset to the beginning.
   *
   * @return A new component with animation time reset to 0
   */
  public AnimatedSpriteComponent withReset() {
    return new AnimatedSpriteComponent(spriteSheetHandle, currentAnimation, currentDirection,
                                      0.0f, playing);
  }
}
