package engine.services.zone.tilemap;

import java.awt.Image;
import java.util.List;

/**
 * A collection of tile definitions, typically sourced from a single image (tilesheet).
 */
public interface Tileset {
  /**
   * @return The name of the tileset.
   */
  String getName();

  /**
   * @return A reference to the full tilesheet image.
   */
  Image getSourceImage();

  /**
   * @return The width of a single tile in pixels.
   */
  int getTileWidth();

  /**
   * @return The height of a single tile in pixels.
   */
  int getTileHeight();

  /**
   * @return The list of all tile definitions in this set.
   */
  List<Tile> getTiles();

  /**
   * Returns the tile definition for a given ID.
   *
   * @param id The tile ID to look up
   * @return The tile with the given ID, or null if not found
   */
  Tile getTileById(int id);
}
