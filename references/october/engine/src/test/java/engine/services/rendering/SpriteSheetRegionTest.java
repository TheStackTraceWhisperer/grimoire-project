package engine.services.rendering;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpriteSheetRegionTest {

  @Test
  void constructor_shouldCreateValidRegion() {
    SpriteSheetRegion region = new SpriteSheetRegion(10, 20, 32, 48);

    assertEquals(10, region.x());
    assertEquals(20, region.y());
    assertEquals(32, region.width());
    assertEquals(48, region.height());
  }

  @Test
  void constructor_shouldThrowExceptionForNegativeX() {
    assertThrows(IllegalArgumentException.class, () ->
        new SpriteSheetRegion(-1, 0, 32, 32));
  }

  @Test
  void constructor_shouldThrowExceptionForNegativeY() {
    assertThrows(IllegalArgumentException.class, () ->
        new SpriteSheetRegion(0, -1, 32, 32));
  }

  @Test
  void constructor_shouldThrowExceptionForZeroWidth() {
    assertThrows(IllegalArgumentException.class, () ->
        new SpriteSheetRegion(0, 0, 0, 32));
  }

  @Test
  void constructor_shouldThrowExceptionForNegativeWidth() {
    assertThrows(IllegalArgumentException.class, () ->
        new SpriteSheetRegion(0, 0, -32, 32));
  }

  @Test
  void constructor_shouldThrowExceptionForZeroHeight() {
    assertThrows(IllegalArgumentException.class, () ->
        new SpriteSheetRegion(0, 0, 32, 0));
  }

  @Test
  void constructor_shouldThrowExceptionForNegativeHeight() {
    assertThrows(IllegalArgumentException.class, () ->
        new SpriteSheetRegion(0, 0, 32, -32));
  }

  @Test
  void getNormalizedCoordinates_shouldReturnCorrectValues() {
    SpriteSheetRegion region = new SpriteSheetRegion(0, 0, 32, 32);
    float[] coords = region.getNormalizedCoordinates(128, 128);

    assertEquals(4, coords.length);
    assertEquals(0.0f, coords[0], 0.001f); // u_min
    assertEquals(0.0f, coords[1], 0.001f); // v_min
    assertEquals(0.25f, coords[2], 0.001f); // u_max
    assertEquals(0.25f, coords[3], 0.001f); // v_max
  }

  @Test
  void getNormalizedCoordinates_shouldHandleOffsetRegion() {
    SpriteSheetRegion region = new SpriteSheetRegion(64, 32, 32, 16);
    float[] coords = region.getNormalizedCoordinates(256, 256);

    assertEquals(0.25f, coords[0], 0.001f); // u_min = 64/256
    assertEquals(0.125f, coords[1], 0.001f); // v_min = 32/256
    assertEquals(0.375f, coords[2], 0.001f); // u_max = 96/256
    assertEquals(0.1875f, coords[3], 0.001f); // v_max = 48/256
  }

  @Test
  void getNormalizedCoordinates_shouldHandleFullTexture() {
    SpriteSheetRegion region = new SpriteSheetRegion(0, 0, 256, 256);
    float[] coords = region.getNormalizedCoordinates(256, 256);

    assertEquals(0.0f, coords[0], 0.001f); // u_min
    assertEquals(0.0f, coords[1], 0.001f); // v_min
    assertEquals(1.0f, coords[2], 0.001f); // u_max
    assertEquals(1.0f, coords[3], 0.001f); // v_max
  }

  @Test
  void equals_shouldReturnTrueForIdenticalRegions() {
    SpriteSheetRegion region1 = new SpriteSheetRegion(10, 20, 30, 40);
    SpriteSheetRegion region2 = new SpriteSheetRegion(10, 20, 30, 40);

    assertEquals(region1, region2);
  }

  @Test
  void equals_shouldReturnFalseForDifferentRegions() {
    SpriteSheetRegion region1 = new SpriteSheetRegion(10, 20, 30, 40);
    SpriteSheetRegion region2 = new SpriteSheetRegion(10, 20, 30, 41);

    assertNotEquals(region1, region2);
  }

  @Test
  void hashCode_shouldBeConsistentForEqualRegions() {
    SpriteSheetRegion region1 = new SpriteSheetRegion(10, 20, 30, 40);
    SpriteSheetRegion region2 = new SpriteSheetRegion(10, 20, 30, 40);

    assertEquals(region1.hashCode(), region2.hashCode());
  }
}
