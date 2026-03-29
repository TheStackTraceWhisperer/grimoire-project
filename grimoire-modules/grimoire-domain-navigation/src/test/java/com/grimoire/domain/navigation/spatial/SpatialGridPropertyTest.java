package com.grimoire.domain.navigation.spatial;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * jqwik property tests for SpatialGrid invariants.
 */
class SpatialGridPropertyTest {

    @Property
    void insertedEntityIsAlwaysNearby(
            @ForAll @DoubleRange(min = -1000, max = 1000) double x,
            @ForAll @DoubleRange(min = -1000, max = 1000) double y,
            @ForAll @StringLength(min = 1, max = 20) String zoneId) {
        var grid = new SpatialGrid(64);

        grid.updateEntity("e1", x, y, zoneId);

        assertThat(grid.getNearbyEntities(x, y, zoneId)).contains("e1");
    }

    @Property
    void removedEntityIsNeverNearby(
            @ForAll @DoubleRange(min = -1000, max = 1000) double x,
            @ForAll @DoubleRange(min = -1000, max = 1000) double y,
            @ForAll @StringLength(min = 1, max = 20) String zoneId) {
        var grid = new SpatialGrid(64);

        grid.updateEntity("e1", x, y, zoneId);
        grid.removeEntity("e1");

        assertThat(grid.getNearbyEntities(x, y, zoneId)).doesNotContain("e1");
        assertThat(grid.getEntityCount()).isZero();
    }

    @Property
    void entityCountTracksInsertions(
            @ForAll @IntRange(min = 1, max = 50) int count) {
        var grid = new SpatialGrid(64);

        for (int i = 0; i < count; i++) {
            grid.updateEntity("e" + i, i * 10.0, i * 10.0, "z1");
        }

        assertThat(grid.getEntityCount()).isEqualTo(count);
    }

    @Property
    void differentZonesAreIsolated(
            @ForAll @DoubleRange(min = 0, max = 100) double x,
            @ForAll @DoubleRange(min = 0, max = 100) double y) {
        var grid = new SpatialGrid(64);

        grid.updateEntity("e1", x, y, "zone-a");
        grid.updateEntity("e2", x, y, "zone-b");

        assertThat(grid.getNearbyEntities(x, y, "zone-a"))
                .contains("e1")
                .doesNotContain("e2");
        assertThat(grid.getNearbyEntities(x, y, "zone-b"))
                .contains("e2")
                .doesNotContain("e1");
    }

    @Property
    void clearRemovesAllEntities(
            @ForAll @IntRange(min = 1, max = 20) int count) {
        var grid = new SpatialGrid(64);

        for (int i = 0; i < count; i++) {
            grid.updateEntity("e" + i, i * 100.0, i * 100.0, "z1");
        }

        grid.clear();

        assertThat(grid.getEntityCount()).isZero();
        assertThat(grid.getCellCount()).isZero();
    }

    @Property
    void updatePositionMovesEntity(
            @ForAll @DoubleRange(min = -500, max = -200) double oldX,
            @ForAll @DoubleRange(min = 200, max = 500) double newX) {
        var grid = new SpatialGrid(64);

        grid.updateEntity("e1", oldX, 0, "z1");
        grid.updateEntity("e1", newX, 0, "z1");

        assertThat(grid.getNearbyEntities(newX, 0, "z1")).contains("e1");
        assertThat(grid.getEntityCount()).isEqualTo(1);
    }
}
