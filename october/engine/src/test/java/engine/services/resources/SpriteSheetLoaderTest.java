package engine.services.resources;

import engine.services.rendering.SpriteAnimation;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SpriteSheetLoader.
 * Note: Tests that load sprite sheets (loadSpriteSheet) are skipped because they require
 * an OpenGL context to create textures, which is not available in unit test environments.
 * Only animation and directional animation loading are tested here as they don't require OpenGL.
 */
class SpriteSheetLoaderTest {

  // Note: loadSpriteSheet tests are skipped because texture loading requires OpenGL context
  // which is not available in unit test environments. These would need integration tests
  // with a proper OpenGL context initialized.
  // Note: loadSpriteSheet tests are skipped because texture loading requires OpenGL context
  // which is not available in unit test environments. These would need integration tests
  // with a proper OpenGL context initialized.

  @Test
  void loadAnimations_shouldLoadValidAnimations() {
    Map<String, SpriteAnimation> animations =
        SpriteSheetLoader.loadAnimations("spritesheets/valid_spritesheet.yml");

    assertNotNull(animations);
    assertEquals(3, animations.size());

    // Verify idle animation
    assertTrue(animations.containsKey("idle"));
    SpriteAnimation idle = animations.get("idle");
    assertEquals("idle", idle.getName());
    assertEquals(1, idle.getFrameCount());
    assertEquals(0.1f, idle.getFrameDuration(), 0.001f);
    assertTrue(idle.isLoop());
    assertEquals("idle_0", idle.getFrameName(0));

    // Verify walk animation
    assertTrue(animations.containsKey("walk"));
    SpriteAnimation walk = animations.get("walk");
    assertEquals("walk", walk.getName());
    assertEquals(3, walk.getFrameCount());
    assertEquals(0.15f, walk.getFrameDuration(), 0.001f);
    assertTrue(walk.isLoop());
    assertEquals("walk_0", walk.getFrameName(0));
    assertEquals("walk_1", walk.getFrameName(1));
    assertEquals("walk_2", walk.getFrameName(2));

    // Verify attack animation (non-looping)
    assertTrue(animations.containsKey("attack"));
    SpriteAnimation attack = animations.get("attack");
    assertEquals("attack", attack.getName());
    assertEquals(1, attack.getFrameCount());
    assertEquals(0.2f, attack.getFrameDuration(), 0.001f);
    assertFalse(attack.isLoop());
  }

  @Test
  void loadAnimations_shouldReturnEmptyMapForNoAnimations() {
    Map<String, SpriteAnimation> animations =
        SpriteSheetLoader.loadAnimations("spritesheets/no_regions.yml");

    assertNotNull(animations);
    assertTrue(animations.isEmpty());
  }

  @Test
  void loadAnimations_shouldThrowExceptionForNonexistentFile() {
    RuntimeException exception = assertThrows(RuntimeException.class, () ->
        SpriteSheetLoader.loadAnimations("spritesheets/nonexistent.yml"));

    assertTrue(exception.getMessage().contains("Failed to load animations from"));
  }

  @Test
  void loadDirectionalAnimations_shouldLoadValidMappings() {
    Map<String, Map<String, String>> dirAnimations =
        SpriteSheetLoader.loadDirectionalAnimations("spritesheets/valid_spritesheet.yml");

    assertNotNull(dirAnimations);
    assertEquals(1, dirAnimations.size());

    assertTrue(dirAnimations.containsKey("walk"));
    Map<String, String> walkMappings = dirAnimations.get("walk");

    assertEquals(4, walkMappings.size());
    assertEquals("walk", walkMappings.get("DOWN"));
    assertEquals("walk", walkMappings.get("UP"));
    assertEquals("walk", walkMappings.get("LEFT"));
    assertEquals("walk", walkMappings.get("RIGHT"));
  }

  @Test
  void loadDirectionalAnimations_shouldReturnEmptyMapForNoDirectionalAnimations() {
    Map<String, Map<String, String>> dirAnimations =
        SpriteSheetLoader.loadDirectionalAnimations("spritesheets/minimal_spritesheet.yml");

    assertNotNull(dirAnimations);
    assertTrue(dirAnimations.isEmpty());
  }

  @Test
  void loadDirectionalAnimations_shouldThrowExceptionForNonexistentFile() {
    RuntimeException exception = assertThrows(RuntimeException.class, () ->
        SpriteSheetLoader.loadDirectionalAnimations("spritesheets/nonexistent.yml"));

    assertTrue(exception.getMessage().contains("Failed to load directional animations from"));
  }

  @Test
  void loadAnimations_shouldHandleAnimationsWithDifferentFrameCounts() {
    Map<String, SpriteAnimation> animations =
        SpriteSheetLoader.loadAnimations("spritesheets/valid_spritesheet.yml");

    SpriteAnimation idle = animations.get("idle");
    assertEquals(1, idle.getFrameCount());

    SpriteAnimation walk = animations.get("walk");
    assertEquals(3, walk.getFrameCount());
  }

  @Test
  void loadAnimations_shouldPreserveFrameOrder() {
    Map<String, SpriteAnimation> animations =
        SpriteSheetLoader.loadAnimations("spritesheets/valid_spritesheet.yml");

    SpriteAnimation walk = animations.get("walk");
    assertEquals("walk_0", walk.getFrameName(0));
    assertEquals("walk_1", walk.getFrameName(1));
    assertEquals("walk_2", walk.getFrameName(2));
  }

  @Test
  void loadAnimations_shouldHandleLeadingSlashInPath() {
    Map<String, SpriteAnimation> animations =
        SpriteSheetLoader.loadAnimations("/spritesheets/valid_spritesheet.yml");

    assertNotNull(animations);
    assertEquals(3, animations.size());
    assertTrue(animations.containsKey("idle"));
  }

  @Test
  void loadDirectionalAnimations_shouldHandleLeadingSlashInPath() {
    Map<String, Map<String, String>> dirAnimations =
        SpriteSheetLoader.loadDirectionalAnimations("/spritesheets/valid_spritesheet.yml");

    assertNotNull(dirAnimations);
    assertEquals(1, dirAnimations.size());
    assertTrue(dirAnimations.containsKey("walk"));
  }
}
