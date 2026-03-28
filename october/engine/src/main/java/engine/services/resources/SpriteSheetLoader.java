package engine.services.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import engine.services.rendering.SpriteAnimation;
import engine.services.rendering.SpriteSheet;
import engine.services.rendering.SpriteSheetRegion;
import engine.services.rendering.Texture;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for loading sprite sheets and animations from YAML configuration files.
 * <p>
 * Expected YAML format:
 * <pre>
 * texture: "textures/player_sheet.png"
 * regions:
 *   idle_0:
 *     x: 0
 *     y: 0
 *     width: 32
 *     height: 32
 *   walk_down_0:
 *     x: 32
 *     y: 0
 *     width: 32
 *     height: 32
 * animations:
 *   idle:
 *     frames: [idle_0]
 *     frameDuration: 0.1
 *     loop: true
 *   walk_down:
 *     frames: [walk_down_0, walk_down_1, walk_down_2, walk_down_3]
 *     frameDuration: 0.15
 *     loop: true
 * directionalAnimations:
 *   walk:
 *     DOWN: walk_down
 *     UP: walk_up
 *     LEFT: walk_left
 *     RIGHT: walk_right
 * </pre>
 */
public class SpriteSheetLoader {

  private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

  /**
   * Loads a sprite sheet from a YAML configuration file.
   *
   * @param yamlPath The classpath path to the YAML configuration file
   * @return A SpriteSheet with all regions defined in the YAML
   */
  public static SpriteSheet loadSpriteSheet(String yamlPath) {
    try {
      SpriteSheetConfig config = loadConfig(yamlPath);
      Texture texture = AssetLoaderUtility.loadTexture(config.texture);
      SpriteSheet spriteSheet = new SpriteSheet(texture);

      // Add all regions
      if (config.regions != null) {
        config.regions.forEach((name, regionData) -> {
          SpriteSheetRegion region = new SpriteSheetRegion(
              regionData.x,
              regionData.y,
              regionData.width,
              regionData.height
          );
          spriteSheet.addRegion(name, region);
        });
      }

      return spriteSheet;
    } catch (IOException e) {
      throw new RuntimeException("Failed to load sprite sheet from: " + yamlPath, e);
    }
  }

  /**
   * Loads sprite animations from a YAML configuration file.
   *
   * @param yamlPath The classpath path to the YAML configuration file
   * @return A map of animation names to SpriteAnimation objects
   */
  public static Map<String, SpriteAnimation> loadAnimations(String yamlPath) {
    try {
      SpriteSheetConfig config = loadConfig(yamlPath);
      Map<String, SpriteAnimation> animations = new HashMap<>();

      if (config.animations != null) {
        config.animations.forEach((name, animData) -> {
          SpriteAnimation animation = new SpriteAnimation(
              name,
              animData.frames != null ? animData.frames : new ArrayList<>(),
              animData.frameDuration,
              animData.loop
          );
          animations.put(name, animation);
        });
      }

      return animations;
    } catch (IOException e) {
      throw new RuntimeException("Failed to load animations from: " + yamlPath, e);
    }
  }

  /**
   * Loads directional animation mappings from a YAML configuration file.
   *
   * @param yamlPath The classpath path to the YAML configuration file
   * @return A map of animation group names to direction-to-animation mappings
   */
  public static Map<String, Map<String, String>> loadDirectionalAnimations(String yamlPath) {
    try {
      SpriteSheetConfig config = loadConfig(yamlPath);
      return config.directionalAnimations != null ? config.directionalAnimations : new HashMap<>();
    } catch (IOException e) {
      throw new RuntimeException("Failed to load directional animations from: " + yamlPath, e);
    }
  }

  private static SpriteSheetConfig loadConfig(String yamlPath) throws IOException {
    String correctedPath = yamlPath.startsWith("/") ? yamlPath.substring(1) : yamlPath;
    try (InputStream is = SpriteSheetLoader.class.getClassLoader().getResourceAsStream(correctedPath)) {
      if (is == null) {
        throw new IOException("Resource not found: " + yamlPath);
      }
      return YAML_MAPPER.readValue(is, SpriteSheetConfig.class);
    }
  }

  /**
   * Internal configuration class for YAML deserialization.
   */
  private static class SpriteSheetConfig {
    public String texture;
    public Map<String, RegionData> regions;
    public Map<String, AnimationData> animations;
    public Map<String, Map<String, String>> directionalAnimations;
  }

  /**
   * Internal region data class for YAML deserialization.
   */
  private static class RegionData {
    public int x;
    public int y;
    public int width;
    public int height;
  }

  /**
   * Internal animation data class for YAML deserialization.
   */
  private static class AnimationData {
    public List<String> frames;
    public float frameDuration;
    public boolean loop;
  }
}
