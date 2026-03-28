package engine.services.zone;

import com.fasterxml.jackson.databind.ObjectMapper;
import engine.services.zone.tilemap.Tilemap;
import engine.services.zone.tilemap.Tilelayer;
import engine.services.zone.tilemap.Tileset;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for loading zones with tilemaps from JSON files.
 */
class TilemapLoadingIT {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void loadZone_shouldLoadTilemapFromJson() throws Exception {
    // Given
    String resourcePath = "/zones/test_tilemap_zone.json";

    // When - Load and parse the JSON directly
    try (InputStream is = TilemapLoadingIT.class.getResourceAsStream(resourcePath)) {
      assertThat(is).isNotNull();

      ZoneService.JsonZone zone = MAPPER.readValue(is, ZoneService.JsonZone.class);

      // Then
      assertThat(zone).isNotNull();
      assertThat(zone.getId()).isEqualTo("test_tilemap_zone");
      assertThat(zone.getName()).isEqualTo("Test Tilemap Zone");

      // Verify tilemap was loaded
      Tilemap tilemap = zone.getTilemap();
      assertThat(tilemap).isNotNull();
      assertThat(tilemap.getWidth()).isEqualTo(10);
      assertThat(tilemap.getHeight()).isEqualTo(10);
      assertThat(tilemap.getTileWidth()).isEqualTo(16);
      assertThat(tilemap.getTileHeight()).isEqualTo(16);

      // Verify tilesets
      assertThat(tilemap.getTilesets()).hasSize(1);
      Tileset tileset = tilemap.getTilesets().get(0);
      assertThat(tileset.getName()).isEqualTo("dungeon-tileset");
      assertThat(tileset.getTileWidth()).isEqualTo(16);
      assertThat(tileset.getTileHeight()).isEqualTo(16);
      assertThat(tileset.getTiles()).hasSize(4);

      // Verify tile properties
      assertThat(tileset.getTileById(0)).isNotNull();
      assertThat(tileset.getTileById(0).getProperties()).containsEntry("type", "floor");
      assertThat(tileset.getTileById(0).getProperties()).containsEntry("collision", false);

      assertThat(tileset.getTileById(6)).isNotNull();
      assertThat(tileset.getTileById(6).getProperties()).containsEntry("type", "wall");
      assertThat(tileset.getTileById(6).getProperties()).containsEntry("collision", true);

      // Verify tilelayers
      assertThat(tilemap.getTilelayers()).hasSize(2);

      Tilelayer backgroundLayer = tilemap.getTilelayers().get(0);
      assertThat(backgroundLayer.getName()).isEqualTo("background");
      assertThat(backgroundLayer.getWidth()).isEqualTo(10);
      assertThat(backgroundLayer.getHeight()).isEqualTo(10);
      assertThat(backgroundLayer.isVisible()).isTrue();
      assertThat(backgroundLayer.getOpacity()).isEqualTo(1.0f);

      int[][] backgroundTiles = backgroundLayer.getTileIds();
      assertThat(backgroundTiles.length).isEqualTo(10);
      assertThat(backgroundTiles[0].length).isEqualTo(10);
      assertThat(backgroundTiles[0][0]).isEqualTo(0); // top-left corner
      assertThat(backgroundTiles[1][1]).isEqualTo(1); // interior floor

      Tilelayer wallsLayer = tilemap.getTilelayers().get(1);
      assertThat(wallsLayer.getName()).isEqualTo("walls");
      assertThat(wallsLayer.getWidth()).isEqualTo(10);
      assertThat(wallsLayer.getHeight()).isEqualTo(10);

      int[][] wallTiles = wallsLayer.getTileIds();
      assertThat(wallTiles[0][0]).isEqualTo(6); // top-left wall
      assertThat(wallTiles[1][1]).isEqualTo(-1); // empty interior
      assertThat(wallTiles[4][4]).isEqualTo(7); // center obstacle

      // Verify tilemap properties
      assertThat(tilemap.getProperties()).containsEntry("theme", "dungeon");
    }
  }

  @Test
  void loadZone_withoutTilemap_shouldHaveNullTilemap() throws Exception {
    // Given
    String resourcePath = "/zones/intro_cutscene_zone.json";

    // When - Load and parse the JSON directly
    try (InputStream is = TilemapLoadingIT.class.getResourceAsStream(resourcePath)) {
      assertThat(is).isNotNull();

      ZoneService.JsonZone zone = MAPPER.readValue(is, ZoneService.JsonZone.class);

      // Then
      assertThat(zone).isNotNull();
      assertThat(zone.getId()).isEqualTo("intro_cutscene_zone");
      assertThat(zone.getTilemap()).isNull(); // intro zone has no tilemap
    }
  }
}
