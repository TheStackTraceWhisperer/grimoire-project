package com.grimoire.domain.navigation;

import com.grimoire.domain.core.component.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AStarPathfinderTest {

    private NavigationGrid grid;

    @BeforeEach
    void setUp() {
        // 320×320 world with 32px tiles = 10×10 grid
        grid = new NavigationGrid(320, 320, 32);
    }

    @Test
    void pathOnEmptyGrid() {
        Optional<List<Position>> result = AStarPathfinder.findPath(16, 16, 272, 272, grid);

        assertThat(result).isPresent();
        List<Position> path = result.orElseThrow();
        assertThat(path).isNotEmpty();
        assertThat(path.getFirst()).isEqualTo(new Position(16, 16));
        assertThat(path.getLast()).isEqualTo(new Position(272, 272));
    }

    @Test
    void sameStartAndTarget() {
        Optional<List<Position>> result = AStarPathfinder.findPath(50, 50, 55, 55, grid);

        assertThat(result).isPresent().hasValueSatisfying(List::isEmpty);
    }

    @Test
    void blockedTargetReturnsEmpty() {
        grid.setBlocked(5, 5);

        assertThat(AStarPathfinder.findPath(16, 16, 176, 176, grid)).isEmpty();
    }

    @Test
    void blockedStartReturnsEmpty() {
        grid.setBlocked(0, 0);

        assertThat(AStarPathfinder.findPath(16, 16, 176, 176, grid)).isEmpty();
    }

    @Test
    void pathAroundObstacle() {
        grid.setBlocked(2, 0);
        grid.setBlocked(2, 1);
        grid.setBlocked(2, 2);

        Optional<List<Position>> result = AStarPathfinder.findPath(16, 16, 144, 16, grid);

        assertThat(result).isPresent();
        List<Position> path = result.orElseThrow();
        assertThat(path).hasSizeGreaterThan(2);
        for (Position pos : path) {
            int[] gc = grid.worldToGrid(pos.x, pos.y);
            assertThat(grid.isBlocked(gc[0], gc[1])).isFalse();
        }
    }

    @Test
    void completelyBlockedReturnsEmpty() {
        for (int x = 0; x < 10; x++) {
            grid.setBlocked(x, 5);
        }

        assertThat(AStarPathfinder.findPath(16, 16, 16, 272, grid)).isEmpty();
    }

    @Test
    void nullGridReturnsEmpty() {
        assertThat(AStarPathfinder.findPath(0, 0, 100, 100, null)).isEmpty();
    }

    @Test
    void diagonalMovementIsEfficient() {
        Optional<List<Position>> result = AStarPathfinder.findPath(16, 16, 112, 112, grid);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow()).hasSizeLessThanOrEqualTo(5);
    }

    @Test
    void narrowPassage() {
        for (int x = 0; x < 10; x++) {
            if (x != 4) {
                grid.setBlocked(x, 5);
            }
        }

        Optional<List<Position>> result = AStarPathfinder.findPath(16, 16, 16, 272, grid);

        assertThat(result).isPresent();
        List<Position> path = result.orElseThrow();
        boolean throughGap = path.stream().anyMatch(p -> {
            int[] gc = grid.worldToGrid(p.x, p.y);
            return gc[0] == 4 && gc[1] == 5;
        });
        assertThat(throughGap).isTrue();
    }

    @Test
    void smoothPathReducesWaypoints() {
        List<Position> path = AStarPathfinder.findPath(16, 16, 144, 144, grid).orElseThrow();

        List<Position> smoothed = AStarPathfinder.smoothPath(path, grid);

        assertThat(smoothed).hasSizeLessThanOrEqualTo(path.size());
        assertThat(smoothed.getFirst()).isEqualTo(path.getFirst());
        assertThat(smoothed.getLast()).isEqualTo(path.getLast());
    }

    @Test
    void smoothPathNullReturnsNull() {
        assertThat(AStarPathfinder.smoothPath(null, grid)).isNull();
    }

    @Test
    void smoothPathNullGridReturnsSame() {
        List<Position> path = AStarPathfinder.findPath(16, 16, 144, 144, grid).orElseThrow();
        assertThat(AStarPathfinder.smoothPath(path, null)).isEqualTo(path);
    }

    @Test
    void lineOfSightClear() {
        assertThat(AStarPathfinder.hasLineOfSight(
                new Position(16, 16), new Position(144, 144), grid)).isTrue();
    }

    @Test
    void lineOfSightBlocked() {
        grid.setBlocked(2, 2);

        assertThat(AStarPathfinder.hasLineOfSight(
                new Position(16, 16), new Position(144, 144), grid)).isFalse();
    }

    @Test
    void doesNotCutCorners() {
        grid.setBlocked(3, 3);
        grid.setBlocked(3, 4);
        grid.setBlocked(4, 3);

        Optional<List<Position>> result = AStarPathfinder.findPath(80, 80, 144, 144, grid);

        assertThat(result).isPresent();
        for (Position pos : result.orElseThrow()) {
            int[] gc = grid.worldToGrid(pos.x, pos.y);
            assertThat(grid.isBlocked(gc[0], gc[1])).isFalse();
        }
    }

    @Test
    void adjacentCellPath() {
        List<Position> path = AStarPathfinder.findPath(16, 16, 48, 16, grid).orElseThrow();

        assertThat(path).hasSize(2);
    }

    @Test
    void largeGrid() {
        var largeGrid = new NavigationGrid(1024, 1024, 32);

        Optional<List<Position>> result = AStarPathfinder.findPath(16, 16, 1008, 1008, largeGrid);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow()).isNotEmpty();
    }

    @Test
    void mazeNavigation() {
        for (int y = 1; y <= 7; y++) {
            if (y != 4) {
                grid.setBlocked(5, y);
            }
        }

        Optional<List<Position>> result = AStarPathfinder.findPath(48, 112, 208, 112, grid);

        assertThat(result).isPresent();
        List<Position> path = result.orElseThrow();
        for (Position pos : path) {
            int[] gc = grid.worldToGrid(pos.x, pos.y);
            assertThat(grid.isBlocked(gc[0], gc[1])).isFalse();
        }
        boolean throughGap = path.stream().anyMatch(p -> {
            int[] gc = grid.worldToGrid(p.x, p.y);
            return gc[0] == 5 && gc[1] == 4;
        });
        assertThat(throughGap).isTrue();
    }
}
