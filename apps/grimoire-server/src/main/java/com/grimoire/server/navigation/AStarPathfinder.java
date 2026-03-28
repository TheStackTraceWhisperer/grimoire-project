package com.grimoire.server.navigation;

import com.grimoire.server.component.Position;

import java.util.*;

/**
 * A* pathfinding algorithm implementation for grid-based navigation.
 * 
 * <p>This implementation supports 8-directional movement (including diagonals)
 * and uses Euclidean distance as the heuristic for smoother paths.</p>
 * 
 * <p>Usage example:
 * <pre>{@code
 *     NavigationGrid grid = ...;
 *     List<Position> path = AStarPathfinder.findPath(
 *         startX, startY, targetX, targetY, grid);
 *     if (path != null) {
 *         // Path found
 *     }
 * }</pre>
 * </p>
 */
public final class AStarPathfinder {
    
    /**
     * Maximum number of nodes to explore before giving up.
     * This prevents infinite loops or extremely long searches.
     */
    private static final int MAX_ITERATIONS = 10000;
    
    /**
     * Cost to move to an adjacent cell (horizontal/vertical).
     */
    private static final double STRAIGHT_COST = 1.0;
    
    /**
     * Cost to move diagonally (sqrt(2)).
     */
    private static final double DIAGONAL_COST = Math.sqrt(2);
    
    /**
     * 8-directional movement offsets (including diagonals).
     */
    private static final int[][] DIRECTIONS = {
        {0, -1},  // Up
        {0, 1},   // Down
        {-1, 0},  // Left
        {1, 0},   // Right
        {-1, -1}, // Up-Left
        {1, -1},  // Up-Right
        {-1, 1},  // Down-Left
        {1, 1}    // Down-Right
    };
    
    private AStarPathfinder() {
        // Utility class
    }
    
    /**
     * Finds a path from start to target position using A* algorithm.
     * 
     * @param startX the starting X coordinate in world space
     * @param startY the starting Y coordinate in world space
     * @param targetX the target X coordinate in world space
     * @param targetY the target Y coordinate in world space
     * @param grid the navigation grid
     * @return list of waypoints (world positions) from start to target, or null if no path found
     */
    public static List<Position> findPath(double startX, double startY, 
                                          double targetX, double targetY, 
                                          NavigationGrid grid) {
        if (grid == null) {
            return null;
        }
        
        int[] startGrid = grid.worldToGrid(startX, startY);
        int[] targetGrid = grid.worldToGrid(targetX, targetY);
        
        int startGx = startGrid[0];
        int startGy = startGrid[1];
        int targetGx = targetGrid[0];
        int targetGy = targetGrid[1];
        
        // Quick check: if start or target is blocked, no path possible
        if (grid.isBlocked(startGx, startGy)) {
            return null;
        }
        if (grid.isBlocked(targetGx, targetGy)) {
            return null;
        }
        
        // If start and target are the same cell, return empty path
        if (startGx == targetGx && startGy == targetGy) {
            return Collections.emptyList();
        }
        
        List<int[]> gridPath = findGridPath(startGx, startGy, targetGx, targetGy, grid);
        
        if (gridPath == null) {
            return null;
        }
        
        return convertToWorldPath(gridPath, grid);
    }
    
    /**
     * Finds a path on the grid level.
     * 
     * @return list of grid coordinates, or null if no path found
     */
    private static List<int[]> findGridPath(int startGx, int startGy, 
                                            int targetGx, int targetGy, 
                                            NavigationGrid grid) {
        // Open set: nodes to be evaluated
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        
        // Closed set: nodes already evaluated
        Set<Long> closedSet = new HashSet<>();
        
        // Map from node key to parent for path reconstruction
        Map<Long, int[]> cameFrom = new HashMap<>();
        
        // Map from node key to g-score (cost from start)
        Map<Long, Double> gScore = new HashMap<>();
        
        Node startNode = new Node(startGx, startGy, 0, heuristic(startGx, startGy, targetGx, targetGy));
        openSet.add(startNode);
        gScore.put(nodeKey(startGx, startGy), 0.0);
        
        int iterations = 0;
        
        while (!openSet.isEmpty() && iterations < MAX_ITERATIONS) {
            iterations++;
            
            Node current = openSet.poll();
            
            // Check if we reached the target
            if (current.x == targetGx && current.y == targetGy) {
                return reconstructPath(cameFrom, current.x, current.y);
            }
            
            long currentKey = nodeKey(current.x, current.y);
            if (closedSet.contains(currentKey)) {
                continue; // Already processed
            }
            closedSet.add(currentKey);
            
            // Explore neighbors
            for (int[] dir : DIRECTIONS) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];
                
                // Skip if blocked or out of bounds
                if (grid.isBlocked(nx, ny)) {
                    continue;
                }
                
                // For diagonal movement, prevent cutting through corners.
                // If either adjacent cardinal cell (horizontal or vertical) is blocked,
                // skip the diagonal move to avoid clipping through wall corners.
                // Note: This allows diagonal moves only when both adjacent cells are walkable.
                if (dir[0] != 0 && dir[1] != 0) {
                    if (grid.isBlocked(current.x + dir[0], current.y) ||
                        grid.isBlocked(current.x, current.y + dir[1])) {
                        continue;
                    }
                }
                
                long neighborKey = nodeKey(nx, ny);
                if (closedSet.contains(neighborKey)) {
                    continue;
                }
                
                double moveCost = (dir[0] != 0 && dir[1] != 0) ? DIAGONAL_COST : STRAIGHT_COST;
                double tentativeG = current.g + moveCost;
                
                double currentG = gScore.getOrDefault(neighborKey, Double.MAX_VALUE);
                if (tentativeG < currentG) {
                    // This path is better
                    cameFrom.put(neighborKey, new int[]{current.x, current.y});
                    gScore.put(neighborKey, tentativeG);
                    
                    double h = heuristic(nx, ny, targetGx, targetGy);
                    openSet.add(new Node(nx, ny, tentativeG, tentativeG + h));
                }
            }
        }
        
        // No path found
        return null;
    }
    
    /**
     * Reconstructs the path from the cameFrom map.
     */
    private static List<int[]> reconstructPath(Map<Long, int[]> cameFrom, int endX, int endY) {
        LinkedList<int[]> path = new LinkedList<>();
        int[] current = {endX, endY};
        path.addFirst(current);
        
        while (cameFrom.containsKey(nodeKey(current[0], current[1]))) {
            current = cameFrom.get(nodeKey(current[0], current[1]));
            path.addFirst(current);
        }
        
        return path;
    }
    
    /**
     * Converts a grid path to world coordinates.
     */
    private static List<Position> convertToWorldPath(List<int[]> gridPath, NavigationGrid grid) {
        List<Position> worldPath = new ArrayList<>(gridPath.size());
        
        for (int[] gridCoord : gridPath) {
            double[] worldCoord = grid.gridToWorld(gridCoord[0], gridCoord[1]);
            worldPath.add(new Position(worldCoord[0], worldCoord[1]));
        }
        
        return worldPath;
    }
    
    /**
     * Calculates the Euclidean distance heuristic.
     */
    private static double heuristic(int x1, int y1, int x2, int y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Creates a unique key for a grid node.
     */
    private static long nodeKey(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }
    
    /**
     * Represents a node in the A* search.
     */
    private static class Node {
        final int x;
        final int y;
        final double g; // Cost from start
        final double f; // Total estimated cost (g + h)
        
        Node(int x, int y, double g, double f) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.f = f;
        }
    }
    
    /**
     * Applies path smoothing using line-of-sight checks.
     * 
     * <p>This method removes unnecessary waypoints by checking if the entity
     * can walk directly from point A to point C without hitting a wall.</p>
     * 
     * @param path the original path (not modified)
     * @param grid the navigation grid
     * @return the smoothed path
     */
    public static List<Position> smoothPath(List<Position> path, NavigationGrid grid) {
        if (path == null || path.size() <= 2 || grid == null) {
            return path;
        }
        
        List<Position> smoothed = new ArrayList<>();
        smoothed.add(path.get(0));
        
        int current = 0;
        
        while (current < path.size() - 1) {
            int furthest = current + 1;
            
            // Find the furthest visible point
            for (int i = current + 2; i < path.size(); i++) {
                if (hasLineOfSight(path.get(current), path.get(i), grid)) {
                    furthest = i;
                }
            }
            
            smoothed.add(path.get(furthest));
            current = furthest;
        }
        
        return smoothed;
    }
    
    /**
     * Checks if there is a clear line of sight between two positions.
     * 
     * <p>Uses Bresenham's line algorithm to check all grid cells along the path.</p>
     * 
     * @param from the starting position
     * @param to the ending position
     * @param grid the navigation grid
     * @return true if there are no obstacles between the two positions
     */
    public static boolean hasLineOfSight(Position from, Position to, NavigationGrid grid) {
        int[] fromGrid = grid.worldToGrid(from.x(), from.y());
        int[] toGrid = grid.worldToGrid(to.x(), to.y());
        
        int x0 = fromGrid[0];
        int y0 = fromGrid[1];
        int x1 = toGrid[0];
        int y1 = toGrid[1];
        
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        
        int x = x0;
        int y = y0;
        
        while (true) {
            if (grid.isBlocked(x, y)) {
                return false;
            }
            
            if (x == x1 && y == y1) {
                break;
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
        
        return true;
    }
}
