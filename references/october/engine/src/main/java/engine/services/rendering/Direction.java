package engine.services.rendering;

/**
 * Represents the eight cardinal and ordinal directions for sprite orientation.
 * <p>
 * This enum is used to map sprite animations to specific facing directions,
 * commonly used in 2D games with directional sprites (e.g., character walking animations).
 * <p>
 * The directions follow standard conventions:
 * <ul>
 *   <li>UP - North (0°)</li>
 *   <li>UP_RIGHT - Northeast (45°)</li>
 *   <li>RIGHT - East (90°)</li>
 *   <li>DOWN_RIGHT - Southeast (135°)</li>
 *   <li>DOWN - South (180°)</li>
 *   <li>DOWN_LEFT - Southwest (225°)</li>
 *   <li>LEFT - West (270°)</li>
 *   <li>UP_LEFT - Northwest (315°)</li>
 * </ul>
 */
public enum Direction {
  /**
   * Facing upward (North).
   */
  UP,

  /**
   * Facing up and to the right (Northeast).
   */
  UP_RIGHT,

  /**
   * Facing right (East).
   */
  RIGHT,

  /**
   * Facing down and to the right (Southeast).
   */
  DOWN_RIGHT,

  /**
   * Facing downward (South).
   */
  DOWN,

  /**
   * Facing down and to the left (Southwest).
   */
  DOWN_LEFT,

  /**
   * Facing left (West).
   */
  LEFT,

  /**
   * Facing up and to the left (Northwest).
   */
  UP_LEFT;

  /**
   * Converts an angle in degrees to the nearest direction.
   *
   * @param angleDegrees The angle in degrees, where 0° is UP and increases clockwise
   * @return The nearest direction
   */
  public static Direction fromAngle(float angleDegrees) {
    // Normalize angle to 0-360 range
    float normalized = ((angleDegrees % 360) + 360) % 360;

    // Divide into 8 segments of 45 degrees each
    float segment = normalized / 45.0f;
    int index = Math.round(segment) % 8;

    return switch (index) {
      case 0 -> UP;
      case 1 -> UP_RIGHT;
      case 2 -> RIGHT;
      case 3 -> DOWN_RIGHT;
      case 4 -> DOWN;
      case 5 -> DOWN_LEFT;
      case 6 -> LEFT;
      case 7 -> UP_LEFT;
      default -> UP; // Should never happen
    };
  }
}
