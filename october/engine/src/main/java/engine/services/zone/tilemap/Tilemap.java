package engine.services.zone.tilemap;

import java.util.List;
import java.util.Map;

/**
 * The complete, multi-layered map structure.
 */
public interface Tilemap {
  /**
   * @return The overall map width in tile units.
   */
  int getWidth();

  /**
   * @return The overall map height in tile units.
   */
  int getHeight();

  /**
   * @return The pixel width of tiles used in this map.
   */
  int getTileWidth();

  /**
   * @return The pixel height of tiles used in this map.
   */
  int getTileHeight();

  /**
   * @return A list of all tilesets used to render this map.
   */
  List<Tileset> getTilesets();

  /**
   * @return The ordered list of layers, from bottom to top.
   */
  List<Tilelayer> getTilelayers();

  /**
   * @return A key-value store for map-level metadata.
   */
  Map<String, Object> getProperties();
}
