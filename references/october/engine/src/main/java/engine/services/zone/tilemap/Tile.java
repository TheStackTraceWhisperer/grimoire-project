package engine.services.zone.tilemap;

import java.awt.Image;
import java.util.Map;

/**
 * Represents a single tile definition from a tileset.
 */
public interface Tile {
  /**
   * @return The unique identifier for this tile within its tileset.
   */
  int getId();

  /**
   * @return A reference to the graphical asset for this tile.
   */
  Image getImage();

  /**
   * @return A key-value store for arbitrary metadata (e.g., "collision": true, "type": "grass").
   */
  Map<String, Object> getProperties();
}
