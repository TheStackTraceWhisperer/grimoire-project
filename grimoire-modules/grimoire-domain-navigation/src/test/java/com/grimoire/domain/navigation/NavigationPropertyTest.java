package com.grimoire.domain.navigation;

import com.grimoire.domain.core.component.Position;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * jqwik property tests for navigation domain invariants.
 */
class NavigationPropertyTest {

    // ── NavigationGrid ──

    @Property
    void setBlockedThenQueryReturnsBlocked(
            @ForAll @IntRange(min = 0, max = 9) int gx,
            @ForAll @IntRange(min = 0, max = 9) int gy) {
        var grid = new NavigationGrid(320, 320, 32);

        grid.setBlocked(gx, gy);

        assertThat(grid.isBlocked(gx, gy)).isTrue();
        assertThat(grid.isWalkable(gx, gy)).isFalse();
    }

    @Property
    void setWalkableRevertsBlocked(
            @ForAll @IntRange(min = 0, max = 9) int gx,
            @ForAll @IntRange(min = 0, max = 9) int gy) {
        var grid = new NavigationGrid(320, 320, 32);

        grid.setBlocked(gx, gy);
        grid.setWalkable(gx, gy);

        assertThat(grid.isWalkable(gx, gy)).isTrue();
    }

    @Property
    void worldToGridRoundTripPreservesCell(
            @ForAll @IntRange(min = 0, max = 9) int gx,
            @ForAll @IntRange(min = 0, max = 9) int gy) {
        var grid = new NavigationGrid(320, 320, 32);

        double[] world = grid.gridToWorld(gx, gy);
        int[] backToGrid = grid.worldToGrid(world[0], world[1]);

        assertThat(backToGrid).containsExactly(gx, gy);
    }

    @Property
    void outOfBoundsIsAlwaysBlocked(
            @ForAll @IntRange(min = -100, max = -1) int gx,
            @ForAll @IntRange(min = -100, max = -1) int gy) {
        var grid = new NavigationGrid(320, 320, 32);

        assertThat(grid.isBlocked(gx, gy)).isTrue();
        assertThat(grid.isWalkable(gx, gy)).isFalse();
    }

    // ── AStarPathfinder ──

    @Property
    void pathOnEmptyGridAlwaysExists(
            @ForAll @IntRange(min = 0, max = 9) int sx,
            @ForAll @IntRange(min = 0, max = 9) int sy,
            @ForAll @IntRange(min = 0, max = 9) int tx,
            @ForAll @IntRange(min = 0, max = 9) int ty) {
        var grid = new NavigationGrid(320, 320, 32);
        double[] sw = grid.gridToWorld(sx, sy);
        double[] tw = grid.gridToWorld(tx, ty);

        var result = AStarPathfinder.findPath(sw[0], sw[1], tw[0], tw[1], grid);

        assertThat(result).isPresent();
    }

    @Property
    void pathNeverTraversesBlockedCells(
            @ForAll @IntRange(min = 0, max = 9) int bx,
            @ForAll @IntRange(min = 0, max = 9) int by) {
        var grid = new NavigationGrid(320, 320, 32);
        grid.setBlocked(bx, by);

        // path from (0,0) to (9,9) — may or may not exist depending on block
        double[] sw = grid.gridToWorld(0, 0);
        double[] tw = grid.gridToWorld(9, 9);

        var result = AStarPathfinder.findPath(sw[0], sw[1], tw[0], tw[1], grid);

        result.ifPresent(path -> {
            for (Position pos : path) {
                int[] gc = grid.worldToGrid(pos.x, pos.y);
                assertThat(grid.isBlocked(gc[0], gc[1])).isFalse();
            }
        });
    }

    @Property
    void sameStartAndTargetReturnsEmptyPath(
            @ForAll @IntRange(min = 0, max = 9) int gx,
            @ForAll @IntRange(min = 0, max = 9) int gy) {
        var grid = new NavigationGrid(320, 320, 32);
        double[] w = grid.gridToWorld(gx, gy);

        var result = AStarPathfinder.findPath(w[0], w[1], w[0], w[1], grid);

        assertThat(result).isPresent().hasValueSatisfying(List::isEmpty);
    }

    @Property
    void smoothedPathIsNoLongerThanOriginal(
            @ForAll @IntRange(min = 0, max = 4) int sx,
            @ForAll @IntRange(min = 5, max = 9) int tx) {
        var grid = new NavigationGrid(320, 320, 32);
        double[] sw = grid.gridToWorld(sx, 0);
        double[] tw = grid.gridToWorld(tx, 0);

        var result = AStarPathfinder.findPath(sw[0], sw[1], tw[0], tw[1], grid);

        result.ifPresent(path -> {
            var smoothed = AStarPathfinder.smoothPath(path, grid);
            assertThat(smoothed.size()).isLessThanOrEqualTo(path.size());
        });
    }

    @Property
    void lineOfSightIsSymmetric(
            @ForAll @IntRange(min = 0, max = 9) int x1,
            @ForAll @IntRange(min = 0, max = 9) int y1,
            @ForAll @IntRange(min = 0, max = 9) int x2,
            @ForAll @IntRange(min = 0, max = 9) int y2) {
        var grid = new NavigationGrid(320, 320, 32);
        double[] w1 = grid.gridToWorld(x1, y1);
        double[] w2 = grid.gridToWorld(x2, y2);
        var from = new Position(w1[0], w1[1]);
        var to = new Position(w2[0], w2[1]);

        assertThat(AStarPathfinder.hasLineOfSight(from, to, grid))
                .isEqualTo(AStarPathfinder.hasLineOfSight(to, from, grid));
    }
}
