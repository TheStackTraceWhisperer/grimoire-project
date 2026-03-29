package engine.services.zone.tilemap;

/**
 * A single layer of a map, represented as a 2D grid of tile IDs.
 */
public interface Tilelayer {
  /**
   * @return The name of the layer (e.g., "background", "foreground").
   */
  String getName();

  /**
   * @return The width of the layer in tile units.
   */
  int getWidth();

  /**
   * @return The height of the layer in tile units.
   */
  int getHeight();

  /**
   * @return A 2D array of tile IDs. A value of 0 or -1 typically represents an empty tile.
   */
  int[][] getTileIds();

  /**
   * @return The visibility state of the layer.
   */
  boolean isVisible();

  /**
   * @return The opacity of the layer, from 0.0 to 1.0.
   */
  float getOpacity();
}
