package com.grimoire.domain.navigation.spatial;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpatialGridTest {

    private SpatialGrid grid;

    @BeforeEach
    void setUp() {
        grid = new SpatialGrid(64);
    }

    @Test
    void addEntity() {
        grid.updateEntity(1, 100, 100, "z1");

        assertThat(grid.getEntityCount()).isEqualTo(1);
        assertThat(grid.getCellCount()).isEqualTo(1);
    }

    @Test
    void nearbyEntitiesSameCell() {
        grid.updateEntity(1, 100, 100, "z1");
        grid.updateEntity(2, 110, 110, "z1");

        Set<Integer> nearby = grid.getNearbyEntities(105, 105, "z1");

        assertThat(nearby).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void nearbyEntitiesAdjacentCell() {
        grid.updateEntity(1, 30, 30, "z1");
        grid.updateEntity(2, 70, 70, "z1");

        assertThat(grid.getNearbyEntities(40, 40, "z1")).contains(1, 2);
    }

    @Test
    void farEntitiesNotReturned() {
        grid.updateEntity(1, 0, 0, "z1");
        grid.updateEntity(2, 200, 200, "z1");

        assertThat(grid.getNearbyEntities(0, 0, "z1")).contains(1).doesNotContain(2);
    }

    @Test
    void differentZonesIsolated() {
        grid.updateEntity(1, 100, 100, "z1");
        grid.updateEntity(2, 100, 100, "z2");

        assertThat(grid.getNearbyEntities(100, 100, "z1")).containsExactly(1);
        assertThat(grid.getNearbyEntities(100, 100, "z2")).containsExactly(2);
    }

    @Test
    void removeEntity() {
        grid.updateEntity(1, 100, 100, "z1");
        grid.removeEntity(1);

        assertThat(grid.getEntityCount()).isZero();
        assertThat(grid.getNearbyEntities(100, 100, "z1")).isEmpty();
    }

    @Test
    void updatePosition() {
        grid.updateEntity(1, 0, 0, "z1");
        grid.updateEntity(1, 200, 200, "z1");

        assertThat(grid.getNearbyEntities(0, 0, "z1")).doesNotContain(1);
        assertThat(grid.getNearbyEntities(200, 200, "z1")).contains(1);
    }

    @Test
    void updateSameCellIsNoOp() {
        grid.updateEntity(1, 10, 10, "z1");
        grid.updateEntity(1, 20, 20, "z1");

        assertThat(grid.getEntityCount()).isEqualTo(1);
        assertThat(grid.getCellCount()).isEqualTo(1);
    }

    @Test
    void clear() {
        grid.updateEntity(1, 100, 100, "z1");
        grid.updateEntity(2, 200, 200, "z1");
        grid.clear();

        assertThat(grid.getEntityCount()).isZero();
        assertThat(grid.getCellCount()).isZero();
    }

    @Test
    void invalidCellSize() {
        assertThatThrownBy(() -> new SpatialGrid(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SpatialGrid(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativeCoordinates() {
        grid.updateEntity(1, -100, -100, "z1");

        assertThat(grid.getNearbyEntities(-100, -100, "z1")).contains(1);
    }
}
