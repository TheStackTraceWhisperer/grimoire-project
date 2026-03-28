package engine.services.zone;

import engine.services.zone.tilemap.Tile;
import engine.services.zone.tilemap.Tilelayer;
import engine.services.zone.tilemap.Tilemap;
import engine.services.zone.tilemap.Tileset;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Tilemap JSON deserialization via ZoneService.
 */
class TilemapDeserializationTest {

  @Test
  void jsonTile_shouldImplementTileInterface() {
    // Given
    ZoneService.JsonTile tile = new ZoneService.JsonTile();
    tile.setId(42);
    Map<String, Object> props = Map.of("collision", true, "type", "wall");
    tile.setProperties(props);

    // When/Then
    assertThat(tile.getId()).isEqualTo(42);
    assertThat(tile.getImage()).isNull();
    assertThat(tile.getProperties()).containsEntry("collision", true);
    assertThat(tile.getProperties()).containsEntry("type", "wall");
  }

  @Test
  void jsonTileset_shouldImplementTilesetInterface() {
    // Given
    ZoneService.JsonTile tile1 = new ZoneService.JsonTile();
    tile1.setId(0);
    tile1.setProperties(Map.of("type", "floor"));

    ZoneService.JsonTile tile2 = new ZoneService.JsonTile();
    tile2.setId(1);
    tile2.setProperties(Map.of("type", "wall"));

    ZoneService.JsonTileset tileset = new ZoneService.JsonTileset();
    tileset.setName("test-tileset");
    tileset.setTileWidth(16);
    tileset.setTileHeight(16);
    tileset.setTiles(java.util.List.of(tile1, tile2));

    // When/Then
    assertThat(tileset.getName()).isEqualTo("test-tileset");
    assertThat(tileset.getTileWidth()).isEqualTo(16);
    assertThat(tileset.getTileHeight()).isEqualTo(16);
    assertThat(tileset.getTiles()).hasSize(2);
    assertThat(tileset.getTileById(0)).isNotNull();
    assertThat(tileset.getTileById(0).getProperties()).containsEntry("type", "floor");
    assertThat(tileset.getTileById(1)).isNotNull();
    assertThat(tileset.getTileById(1).getProperties()).containsEntry("type", "wall");
    assertThat(tileset.getTileById(99)).isNull();
  }

  @Test
  void jsonTilelayer_shouldImplementTilelayerInterface() {
    // Given
    int[][] tileIds = {
      {0, 1, 2},
      {3, 4, 5},
      {6, 7, 8}
    };

    ZoneService.JsonTilelayer layer = new ZoneService.JsonTilelayer();
    layer.setName("background");
    layer.setWidth(3);
    layer.setHeight(3);
    layer.setTileIds(tileIds);
    layer.setVisible(true);
    layer.setOpacity(0.8f);

    // When/Then
    assertThat(layer.getName()).isEqualTo("background");
    assertThat(layer.getWidth()).isEqualTo(3);
    assertThat(layer.getHeight()).isEqualTo(3);
    assertThat(layer.getTileIds()).isEqualTo(tileIds);
    assertThat(layer.isVisible()).isTrue();
    assertThat(layer.getOpacity()).isEqualTo(0.8f);
  }

  @Test
  void jsonTilelayer_shouldHaveDefaultVisibilityAndOpacity() {
    // Given
    ZoneService.JsonTilelayer layer = new ZoneService.JsonTilelayer();
    layer.setName("test");
    layer.setWidth(1);
    layer.setHeight(1);
    layer.setTileIds(new int[][]{{0}});

    // When/Then - defaults
    assertThat(layer.isVisible()).isTrue();
    assertThat(layer.getOpacity()).isEqualTo(1.0f);
  }

  @Test
  void jsonTilemap_shouldImplementTilemapInterface() {
    // Given
    ZoneService.JsonTileset tileset = new ZoneService.JsonTileset();
    tileset.setName("test-set");
    tileset.setTileWidth(16);
    tileset.setTileHeight(16);

    ZoneService.JsonTilelayer layer = new ZoneService.JsonTilelayer();
    layer.setName("background");
    layer.setWidth(10);
    layer.setHeight(10);
    layer.setTileIds(new int[10][10]);

    ZoneService.JsonTilemap tilemap = new ZoneService.JsonTilemap();
    tilemap.setWidth(10);
    tilemap.setHeight(10);
    tilemap.setTileWidth(16);
    tilemap.setTileHeight(16);
    tilemap.setTilesets(java.util.List.of(tileset));
    tilemap.setTilelayers(java.util.List.of(layer));
    tilemap.setProperties(Map.of("theme", "dungeon"));

    // When/Then
    assertThat(tilemap.getWidth()).isEqualTo(10);
    assertThat(tilemap.getHeight()).isEqualTo(10);
    assertThat(tilemap.getTileWidth()).isEqualTo(16);
    assertThat(tilemap.getTileHeight()).isEqualTo(16);
    assertThat(tilemap.getTilesets()).hasSize(1);
    assertThat(tilemap.getTilelayers()).hasSize(1);
    assertThat(tilemap.getProperties()).containsEntry("theme", "dungeon");
  }

  @Test
  void jsonTilemap_shouldReturnEmptyCollectionsWhenNull() {
    // Given
    ZoneService.JsonTilemap tilemap = new ZoneService.JsonTilemap();

    // When/Then
    assertThat(tilemap.getTilesets()).isEmpty();
    assertThat(tilemap.getTilelayers()).isEmpty();
    assertThat(tilemap.getProperties()).isEmpty();
  }

  @Test
  void jsonZone_shouldReturnTilemapWhenSet() {
    // Given
    ZoneService.JsonTilemap tilemap = new ZoneService.JsonTilemap();
    tilemap.setWidth(5);
    tilemap.setHeight(5);
    tilemap.setTileWidth(16);
    tilemap.setTileHeight(16);

    ZoneService.JsonZone zone = new ZoneService.JsonZone();
    zone.setId("test_zone");
    zone.setName("Test Zone");
    zone.setTilemap(tilemap);

    // When
    Tilemap result = zone.getTilemap();

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getWidth()).isEqualTo(5);
    assertThat(result.getHeight()).isEqualTo(5);
  }
}
