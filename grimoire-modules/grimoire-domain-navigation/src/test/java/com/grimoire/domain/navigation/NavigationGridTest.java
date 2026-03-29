package com.grimoire.domain.navigation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NavigationGridTest {

    @Test
    void constructorWithValidDimensions() {
        var grid = new NavigationGrid(640, 480, 32);

        assertThat(grid.getTileSize()).isEqualTo(32);
        assertThat(grid.getGridWidth()).isEqualTo(20);
        assertThat(grid.getGridHeight()).isEqualTo(15);
    }

    @Test
    void constructorWithDefaultTileSize() {
        var grid = new NavigationGrid(640, 480);

        assertThat(grid.getTileSize()).isEqualTo(NavigationGrid.DEFAULT_TILE_SIZE);
    }

    @Test
    void constructorCeilsNonEvenDimensions() {
        var grid = new NavigationGrid(100, 100, 32);

        assertThat(grid.getGridWidth()).isEqualTo(4);
        assertThat(grid.getGridHeight()).isEqualTo(4);
    }

    @Test
    void constructorRejectsInvalidArgs() {
        assertThatThrownBy(() -> new NavigationGrid(0, 480, 32)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NavigationGrid(640, 0, 32)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NavigationGrid(640, 480, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void worldToGridConversion() {
        var grid = new NavigationGrid(640, 480, 32);

        assertThat(grid.worldToGrid(16, 16)).containsExactly(0, 0);
        assertThat(grid.worldToGrid(32, 32)).containsExactly(1, 1);
        assertThat(grid.worldToGrid(100, 200)).containsExactly(3, 6);
    }

    @Test
    void gridToWorldConversion() {
        var grid = new NavigationGrid(640, 480, 32);

        assertThat(grid.gridToWorld(0, 0)).containsExactly(16.0, 16.0);
        assertThat(grid.gridToWorld(1, 1)).containsExactly(48.0, 48.0);
    }

    @Test
    void roundTripConversion() {
        var grid = new NavigationGrid(640, 480, 32);

        for (int gx = 0; gx < 5; gx++) {
            for (int gy = 0; gy < 5; gy++) {
                double[] world = grid.gridToWorld(gx, gy);
                int[] back = grid.worldToGrid(world[0], world[1]);
                assertThat(back).containsExactly(gx, gy);
            }
        }
    }

    @Test
    void newGridIsEntirelyWalkable() {
        var grid = new NavigationGrid(320, 320, 32);

        assertThat(grid.getBlockedCount()).isZero();
        assertThat(grid.isWalkable(0, 0)).isTrue();
        assertThat(grid.isBlocked(0, 0)).isFalse();
    }

    @Test
    void setBlockedAndClear() {
        var grid = new NavigationGrid(640, 480, 32);

        grid.setBlocked(5, 10);
        assertThat(grid.isBlocked(5, 10)).isTrue();
        assertThat(grid.getBlockedCount()).isEqualTo(1);

        grid.setWalkable(5, 10);
        assertThat(grid.isWalkable(5, 10)).isTrue();
        assertThat(grid.getBlockedCount()).isZero();
    }

    @Test
    void outOfBoundsIsBlocked() {
        var grid = new NavigationGrid(640, 480, 32);

        assertThat(grid.isBlocked(-1, 0)).isTrue();
        assertThat(grid.isBlocked(0, -1)).isTrue();
        assertThat(grid.isBlocked(grid.getGridWidth(), 0)).isTrue();
        assertThat(grid.isWalkable(-1, 0)).isFalse();
    }

    @Test
    void setBlockedOutOfBoundsIsIgnored() {
        var grid = new NavigationGrid(640, 480, 32);

        grid.setBlocked(-1, 0);
        grid.setBlocked(grid.getGridWidth(), 0);

        assertThat(grid.getBlockedCount()).isZero();
    }

    @Test
    void markAreaBlocked() {
        var grid = new NavigationGrid(640, 480, 32);

        grid.markAreaBlocked(64, 64, 64, 64);

        // Expected: cells (1,1) to (3,3) = 9 cells
        assertThat(grid.getBlockedCount()).isEqualTo(9);
        assertThat(grid.isWalkable(0, 0)).isTrue();
        assertThat(grid.isWalkable(4, 4)).isTrue();
    }

    @Test
    void markAreaWalkable() {
        var grid = new NavigationGrid(640, 480, 32);

        for (int x = 1; x <= 3; x++) {
            for (int y = 1; y <= 3; y++) {
                grid.setBlocked(x, y);
            }
        }
        assertThat(grid.getBlockedCount()).isEqualTo(9);

        grid.markAreaWalkable(64, 64, 64, 64);
        assertThat(grid.getBlockedCount()).isZero();
    }

    @Test
    void clearResetsAllCells() {
        var grid = new NavigationGrid(640, 480, 32);

        grid.setBlocked(0, 0);
        grid.setBlocked(5, 5);
        grid.clear();

        assertThat(grid.getBlockedCount()).isZero();
    }

    @Test
    void isValidCell() {
        var grid = new NavigationGrid(320, 240, 32);

        assertThat(grid.isValidCell(0, 0)).isTrue();
        assertThat(grid.isValidCell(9, 7)).isTrue();
        assertThat(grid.isValidCell(-1, 0)).isFalse();
        assertThat(grid.isValidCell(10, 0)).isFalse();
    }
}
