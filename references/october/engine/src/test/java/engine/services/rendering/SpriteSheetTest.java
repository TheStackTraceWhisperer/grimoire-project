package engine.services.rendering;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class SpriteSheetTest {

  @Mock
  private Texture mockTexture;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void constructor_shouldCreateEmptySpriteSheet() {
    SpriteSheet sheet = new SpriteSheet(mockTexture);

    assertNotNull(sheet);
    assertEquals(mockTexture, sheet.getTexture());
    assertTrue(sheet.getRegions().isEmpty());
  }

  @Test
  void constructor_shouldThrowExceptionForNullTexture() {
    assertThrows(NullPointerException.class, () -> new SpriteSheet(null));
  }

  @Test
  void addRegion_shouldAddRegionSuccessfully() {
    SpriteSheet sheet = new SpriteSheet(mockTexture);
    SpriteSheetRegion region = new SpriteSheetRegion(0, 0, 32, 32);

    SpriteSheet result = sheet.addRegion("test", region);

    assertSame(sheet, result); // Check method chaining
    assertTrue(sheet.hasRegion("test"));
    assertEquals(region, sheet.getRegion("test"));
  }

  @Test
  void addRegion_shouldThrowExceptionForNullName() {
    SpriteSheet sheet = new SpriteSheet(mockTexture);
    SpriteSheetRegion region = new SpriteSheetRegion(0, 0, 32, 32);

    assertThrows(NullPointerException.class, () -> sheet.addRegion(null, region));
  }

  @Test
  void addRegion_shouldThrowExceptionForNullRegion() {
    SpriteSheet sheet = new SpriteSheet(mockTexture);

    assertThrows(NullPointerException.class, () -> sheet.addRegion("test", null));
  }

  @Test
  void addRegion_shouldOverwriteExistingRegion() {
    SpriteSheet sheet = new SpriteSheet(mockTexture);
    SpriteSheetRegion region1 = new SpriteSheetRegion(0, 0, 32, 32);
    SpriteSheetRegion region2 = new SpriteSheetRegion(32, 32, 16, 16);

    sheet.addRegion("test", region1);
    sheet.addRegion("test", region2);

    assertEquals(region2, sheet.getRegion("test"));
    assertEquals(1, sheet.getRegions().size());
  }

  @Test
  void getRegion_shouldReturnNullForNonExistentRegion() {
    SpriteSheet sheet = new SpriteSheet(mockTexture);

    assertNull(sheet.getRegion("nonexistent"));
  }

  @Test
  void hasRegion_shouldReturnTrueForExistingRegion() {
    SpriteSheet sheet = new SpriteSheet(mockTexture);
    SpriteSheetRegion region = new SpriteSheetRegion(0, 0, 32, 32);

    sheet.addRegion("test", region);

    assertTrue(sheet.hasRegion("test"));
  }

  @Test
  void hasRegion_shouldReturnFalseForNonExistentRegion() {
    SpriteSheet sheet = new SpriteSheet(mockTexture);

    assertFalse(sheet.hasRegion("nonexistent"));
  }

  @Test
  void getRegions_shouldReturnUnmodifiableMap() {
    SpriteSheet sheet = new SpriteSheet(mockTexture);
    SpriteSheetRegion region = new SpriteSheetRegion(0, 0, 32, 32);
    sheet.addRegion("test", region);

    var regions = sheet.getRegions();

    assertThrows(UnsupportedOperationException.class, () ->
        regions.put("new", new SpriteSheetRegion(64, 64, 32, 32)));
  }

  @Test
  void addRegion_shouldSupportMethodChaining() {
    SpriteSheet sheet = new SpriteSheet(mockTexture);
    SpriteSheetRegion region1 = new SpriteSheetRegion(0, 0, 32, 32);
    SpriteSheetRegion region2 = new SpriteSheetRegion(32, 0, 32, 32);
    SpriteSheetRegion region3 = new SpriteSheetRegion(64, 0, 32, 32);

    sheet.addRegion("r1", region1)
         .addRegion("r2", region2)
         .addRegion("r3", region3);

    assertEquals(3, sheet.getRegions().size());
    assertTrue(sheet.hasRegion("r1"));
    assertTrue(sheet.hasRegion("r2"));
    assertTrue(sheet.hasRegion("r3"));
  }
}
