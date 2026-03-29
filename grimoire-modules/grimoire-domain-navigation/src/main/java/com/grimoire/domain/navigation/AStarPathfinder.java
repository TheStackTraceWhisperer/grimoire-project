package com.grimoire.domain.navigation;

import com.grimoire.domain.core.component.Position;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * A* pathfinding algorithm for grid-based navigation.
 *
 * <p>
 * Supports 8-directional movement (including diagonals) and uses Euclidean
 * distance as the heuristic. Corner-cutting through blocked diagonal cells is
 * prevented.
 * </p>
 *
 * <p>
 * All methods are static and side-effect-free.
 * </p>
 */
public final class AStarPathfinder {

    /** Maximum nodes to explore before aborting the search. */
    private static final int MAX_ITERATIONS = 10_000;

    /** Movement cost for horizontal/vertical steps. */
    private static final double STRAIGHT_COST = 1.0;

    /** Movement cost for diagonal steps (√2). */
    private static final double DIAGONAL_COST = Math.sqrt(2);

    /** 8-directional movement offsets (cardinal + diagonal). */
    private static final int[][] DIRECTIONS = {
            {0, -1}, {0, 1}, {-1, 0}, {1, 0},
            {-1, -1}, {1, -1}, {-1, 1}, {1, 1}
    };

    private AStarPathfinder() {
    }

    /**
     * Finds a path from start to target using the A* algorithm.
     *
     * @param startX
     *            start X in world space
     * @param startY
     *            start Y in world space
     * @param targetX
     *            target X in world space
     * @param targetY
     *            target Y in world space
     * @param grid
     *            the navigation grid
     * @return an optional containing the waypoint list, or empty if no path exists
     */
    public static Optional<List<Position>> findPath(double startX, double startY,
            double targetX, double targetY,
            NavigationGrid grid) {
        if (grid == null) {
            return Optional.empty();
        }

        int[] sg = grid.worldToGrid(startX, startY);
        int[] tg = grid.worldToGrid(targetX, targetY);

        if (grid.isBlocked(sg[0], sg[1]) || grid.isBlocked(tg[0], tg[1])) {
            return Optional.empty();
        }
        if (sg[0] == tg[0] && sg[1] == tg[1]) {
            return Optional.of(List.of());
        }

        return searchGrid(sg[0], sg[1], tg[0], tg[1], grid)
                .map(gridPath -> toWorldPath(gridPath, grid));
    }

    /**
     * Smooths a path by removing unnecessary waypoints via line-of-sight checks.
     *
     * @param path
     *            the original path (not modified)
     * @param grid
     *            the navigation grid
     * @return the smoothed path, or the original if smoothing is not applicable
     */
    public static List<Position> smoothPath(List<Position> path, NavigationGrid grid) {
        if (path == null || path.size() <= 2 || grid == null) {
            return path;
        }
        List<Position> smoothed = new ArrayList<>();
        smoothed.add(path.getFirst());
        int current = 0;
        while (current < path.size() - 1) {
            int furthest = findFurthestVisible(path, current, grid);
            smoothed.add(path.get(furthest));
            current = furthest;
        }
        return smoothed;
    }

    /**
     * Checks clear line of sight between two positions using Bresenham's algorithm.
     *
     * @param from
     *            the start position
     * @param to
     *            the end position
     * @param grid
     *            the navigation grid
     * @return {@code true} if no blocked cells lie between the two positions
     */
    public static boolean hasLineOfSight(Position from, Position to, NavigationGrid grid) {
        int[] fg = grid.worldToGrid(from.x(), from.y());
        int[] tg = grid.worldToGrid(to.x(), to.y());

        int x = fg[0];
        int y = fg[1];
        int x1 = tg[0];
        int y1 = tg[1];
        int dx = Math.abs(x1 - x);
        int dy = Math.abs(y1 - y);
        int sx = x < x1 ? 1 : -1;
        int sy = y < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if (grid.isBlocked(x, y)) {
                return false;
            }
            if (x == x1 && y == y1) {
                return true;
            }
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

    // ── internal helpers ──

    private static int findFurthestVisible(List<Position> path, int current, NavigationGrid grid) {
        int furthest = current + 1;
        for (int i = current + 2; i < path.size(); i++) {
            if (hasLineOfSight(path.get(current), path.get(i), grid)) {
                furthest = i;
            }
        }
        return furthest;
    }

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private static Optional<List<int[]>> searchGrid(int sx, int sy, int tx, int ty,
            NavigationGrid grid) {
        Queue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(Node::f));
        Set<Long> closed = new HashSet<>();
        Map<Long, int[]> cameFrom = new HashMap<>();
        Map<Long, Double> gScore = new HashMap<>();

        open.add(new Node(sx, sy, 0, heuristic(sx, sy, tx, ty)));
        gScore.put(key(sx, sy), 0.0);

        int iterations = 0;
        while (!open.isEmpty() && iterations < MAX_ITERATIONS) {
            iterations++;
            Node cur = open.poll();
            if (cur.x() == tx && cur.y() == ty) {
                return Optional.of(reconstruct(cameFrom, cur.x(), cur.y()));
            }
            long ck = key(cur.x(), cur.y());
            if (closed.contains(ck)) {
                continue;
            }
            closed.add(ck);

            exploreNeighbours(cur, tx, ty, grid, open, closed, cameFrom, gScore);
        }
        return Optional.empty();
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static void exploreNeighbours(Node cur, int tx, int ty, NavigationGrid grid,
            Queue<Node> open, Set<Long> closed,
            Map<Long, int[]> cameFrom, Map<Long, Double> gScore) {
        for (int[] dir : DIRECTIONS) {
            int nx = cur.x() + dir[0];
            int ny = cur.y() + dir[1];
            if (grid.isBlocked(nx, ny)) {
                continue;
            }
            if (isDiagonal(dir) && isCornerBlocked(cur, dir, grid)) {
                continue;
            }
            long nk = key(nx, ny);
            if (closed.contains(nk)) {
                continue;
            }
            double cost = isDiagonal(dir) ? DIAGONAL_COST : STRAIGHT_COST;
            double tentG = cur.g() + cost;
            if (tentG < gScore.getOrDefault(nk, Double.MAX_VALUE)) {
                cameFrom.put(nk, new int[]{cur.x(), cur.y()});
                gScore.put(nk, tentG);
                open.add(new Node(nx, ny, tentG, tentG + heuristic(nx, ny, tx, ty)));
            }
        }
    }

    @SuppressWarnings("PMD.UseVarargs")
    private static boolean isDiagonal(int[] dir) {
        return dir[0] != 0 && dir[1] != 0;
    }

    private static boolean isCornerBlocked(Node cur, int[] dir, NavigationGrid grid) {
        return grid.isBlocked(cur.x() + dir[0], cur.y())
                || grid.isBlocked(cur.x(), cur.y() + dir[1]);
    }

    private static List<int[]> reconstruct(Map<Long, int[]> cameFrom, int ex, int ey) {
        Deque<int[]> path = new ArrayDeque<>();
        int[] cur = {ex, ey};
        path.addFirst(cur);
        while (cameFrom.containsKey(key(cur[0], cur[1]))) {
            cur = cameFrom.get(key(cur[0], cur[1]));
            path.addFirst(cur);
        }
        return new ArrayList<>(path);
    }

    private static List<Position> toWorldPath(List<int[]> gridPath, NavigationGrid grid) {
        List<Position> result = new ArrayList<>(gridPath.size());
        for (int[] gc : gridPath) {
            double[] wc = grid.gridToWorld(gc[0], gc[1]);
            result.add(new Position(wc[0], wc[1]));
        }
        return result;
    }

    private static double heuristic(int x1, int y1, int x2, int y2) {
        double dx = (double) x2 - x1;
        double dy = (double) y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static long key(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    private record Node(int x, int y, double g, double f) {
    }
}
