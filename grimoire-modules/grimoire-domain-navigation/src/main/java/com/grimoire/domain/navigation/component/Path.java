package com.grimoire.domain.navigation.component;

import com.grimoire.domain.core.component.Component;
import com.grimoire.domain.core.component.Position;

import java.util.List;

/**
 * Component storing an entity's current navigation path.
 *
 * <p>
 * The component is immutable — to advance to the next waypoint, create a new
 * {@code Path} via {@link #advanceToNextWaypoint()}.
 * </p>
 *
 * @param waypoints
 *            immutable list of positions to follow
 * @param currentIndex
 *            index of the current waypoint
 * @param targetEntityId
 *            ID of the entity being followed, or {@code null} for fixed paths
 * @param lastCalculationTick
 *            the game tick when this path was calculated
 */
public record Path(
        List<Position> waypoints,
        int currentIndex,
        String targetEntityId,
        long lastCalculationTick) implements Component {

    // Compact constructor — ensures the waypoints list is immutable.
    public Path {
        waypoints = waypoints == null ? List.of() : List.copyOf(waypoints);
    }

    /**
     * Creates a path starting at index 0 with a target entity.
     *
     * @param waypointList
     *            the waypoints
     * @param targetEntityId
     *            the entity being pursued
     * @param lastCalculationTick
     *            tick when the path was computed
     * @return a new Path
     */
    public static Path fromList(List<Position> waypointList, String targetEntityId, long lastCalculationTick) {
        return new Path(List.copyOf(waypointList), 0, targetEntityId, lastCalculationTick);
    }

    /**
     * Creates a path with no target entity.
     *
     * @param waypointList
     *            the waypoints
     * @param lastCalculationTick
     *            tick when the path was computed
     * @return a new Path
     */
    public static Path fromList(List<Position> waypointList, long lastCalculationTick) {
        return fromList(waypointList, null, lastCalculationTick);
    }

    /** Returns {@code true} if there are no more waypoints to follow. */
    public boolean isEmpty() {
        return currentIndex >= waypoints.size();
    }

    /** Returns the current waypoint, or {@code null} if the path is empty. */
    public Position getCurrentWaypoint() {
        if (isEmpty()) {
            return null;
        }
        return waypoints.get(currentIndex);
    }

    /** Returns a new Path advanced to the next waypoint. */
    public Path advanceToNextWaypoint() {
        return new Path(waypoints, currentIndex + 1, targetEntityId, lastCalculationTick);
    }

    /** Returns the number of remaining waypoints (including current). */
    public int remainingWaypoints() {
        return Math.max(0, waypoints.size() - currentIndex);
    }

    /** Returns the total number of waypoints. */
    public int size() {
        return waypoints.size();
    }

    /** Returns the last waypoint, or {@code null} if the path is empty. */
    public Position getLastWaypoint() {
        if (waypoints.isEmpty()) {
            return null;
        }
        return waypoints.getLast();
    }
}
