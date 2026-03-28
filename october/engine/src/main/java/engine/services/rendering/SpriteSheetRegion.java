package engine.services.rendering;

import java.util.Objects;

/**
 * Defines a rectangular region within a sprite sheet texture.
 * This record represents the coordinates and dimensions of a single sprite frame
 * within a larger sprite sheet image.
 * <p>
 * Coordinates are in pixel space, with (0,0) at the top-left corner of the sprite sheet.
 */
public record SpriteSheetRegion(int x, int y, int width, int height) {

  /**
   * Compact constructor that validates region dimensions.
   */
  public SpriteSheetRegion {
    if (width <= 0) {
      throw new IllegalArgumentException("Width must be positive, got: " + width);
    }
    if (height <= 0) {
      throw new IllegalArgumentException("Height must be positive, got: " + height);
    }
    if (x < 0) {
      throw new IllegalArgumentException("X coordinate must be non-negative, got: " + x);
    }
    if (y < 0) {
      throw new IllegalArgumentException("Y coordinate must be non-negative, got: " + y);
    }
  }

  /**
   * Returns the normalized texture coordinates for this region within a sprite sheet.
   *
   * @param sheetWidth The total width of the sprite sheet texture
   * @param sheetHeight The total height of the sprite sheet texture
   * @return An array of 4 floats: [u_min, v_min, u_max, v_max] in range [0.0, 1.0]
   */
  public float[] getNormalizedCoordinates(int sheetWidth, int sheetHeight) {
    float uMin = (float) x / sheetWidth;
    float vMin = (float) y / sheetHeight;
    float uMax = (float) (x + width) / sheetWidth;
    float vMax = (float) (y + height) / sheetHeight;
    return new float[]{uMin, vMin, uMax, vMax};
  }
}
