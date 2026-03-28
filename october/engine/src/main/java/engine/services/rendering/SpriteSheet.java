package engine.services.rendering;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a sprite sheet texture with named regions.
 * <p>
 * A sprite sheet is a single texture that contains multiple sprite images arranged in a grid.
 * This class manages the texture and provides access to individual sprite regions by name.
 * <p>
 * Example usage:
 * <pre>
 * SpriteSheet sheet = new SpriteSheet(texture);
 * sheet.addRegion("idle_0", new SpriteSheetRegion(0, 0, 32, 32));
 * sheet.addRegion("walk_0", new SpriteSheetRegion(32, 0, 32, 32));
 * </pre>
 */
public class SpriteSheet {

  private final Texture texture;
  private final Map<String, SpriteSheetRegion> regions;

  /**
   * Creates a new sprite sheet with the given texture.
   *
   * @param texture The underlying texture containing the sprite sheet image
   */
  public SpriteSheet(Texture texture) {
    this.texture = Objects.requireNonNull(texture, "texture must not be null");
    this.regions = new HashMap<>();
  }

  /**
   * Adds a named region to this sprite sheet.
   *
   * @param name The unique name for this region
   * @param region The region coordinates within the sprite sheet
   * @return This sprite sheet for method chaining
   */
  public SpriteSheet addRegion(String name, SpriteSheetRegion region) {
    Objects.requireNonNull(name, "region name must not be null");
    Objects.requireNonNull(region, "region must not be null");
    regions.put(name, region);
    return this;
  }

  /**
   * Gets a region by name.
   *
   * @param name The name of the region to retrieve
   * @return The sprite sheet region, or null if not found
   */
  public SpriteSheetRegion getRegion(String name) {
    return regions.get(name);
  }

  /**
   * Gets the underlying texture.
   *
   * @return The sprite sheet texture
   */
  public Texture getTexture() {
    return texture;
  }

  /**
   * Gets all region names defined in this sprite sheet.
   *
   * @return An unmodifiable map of region names to regions
   */
  public Map<String, SpriteSheetRegion> getRegions() {
    return Collections.unmodifiableMap(regions);
  }

  /**
   * Checks if a region with the given name exists.
   *
   * @param name The region name to check
   * @return True if the region exists
   */
  public boolean hasRegion(String name) {
    return regions.containsKey(name);
  }
}
