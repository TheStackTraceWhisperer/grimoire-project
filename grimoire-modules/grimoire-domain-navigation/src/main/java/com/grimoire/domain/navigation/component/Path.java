package com.grimoire.domain.navigation.component;

import com.grimoire.domain.core.component.Component;
import com.grimoire.domain.core.component.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Component storing an entity's current navigation path.
 *
 * <p>
 * Mutable POJO — advance to the next waypoint by calling
 * {@link #advanceToNextWaypoint()} which mutates the index in place.
 * </p>
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidFieldNameMatchingMethodName"})
public class Path implements Component {

    /** Waypoints to follow. */
    private final List<Position> waypoints;

    /** Index of the current waypoint. */
    public int currentIndex;

    /** ID of the entity being followed, or -1 for fixed paths. */
    public int targetEntityId;

    /** The game tick when this path was calculated. */
    public long lastCalculationTick;

    /** No-arg constructor for array pre-allocation. */
    public Path() {
        this.waypoints = new ArrayList<>();
        this.currentIndex = 0;
        this.targetEntityId = -1;
        this.lastCalculationTick = 0;
    }

    /**
     * Creates a path with the given waypoints.
     *
     * @param waypoints
     *            the waypoints
     * @param currentIndex
     *            index of the current waypoint
     * @param targetEntityId
     *            the entity being pursued, or -1
     * @param lastCalculationTick
     *            tick when the path was computed
     */
    public Path(List<Position> waypoints, int currentIndex, int targetEntityId,
            long lastCalculationTick) {
        this.waypoints = waypoints == null ? new ArrayList<>() : new ArrayList<>(waypoints);
        this.currentIndex = currentIndex;
        this.targetEntityId = targetEntityId;
        this.lastCalculationTick = lastCalculationTick;
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
    public static Path fromList(List<Position> waypointList, int targetEntityId,
            long lastCalculationTick) {
        return new Path(waypointList, 0, targetEntityId, lastCalculationTick);
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
        return fromList(waypointList, -1, lastCalculationTick);
    }

    /**
     * Replaces the current waypoints and resets the index.
     *
     * @param newWaypoints
     *            the new waypoints
     * @param newTargetEntityId
     *            the new target entity ID
     * @param newLastCalcTick
     *            the new calculation tick
     */
    public void update(List<Position> newWaypoints, int newTargetEntityId,
            long newLastCalcTick) {
        this.waypoints.clear();
        if (newWaypoints != null) {
            this.waypoints.addAll(newWaypoints);
        }
        this.currentIndex = 0;
        this.targetEntityId = newTargetEntityId;
        this.lastCalculationTick = newLastCalcTick;
    }

    /** Returns an unmodifiable view of the waypoints. */
    public List<Position> waypoints() {
        return Collections.unmodifiableList(waypoints);
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

    /** Advances to the next waypoint in place. */
    public void advanceToNextWaypoint() {
        currentIndex++;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Path p)) {
            return false;
        }
        return currentIndex == p.currentIndex
                && targetEntityId == p.targetEntityId
                && lastCalculationTick == p.lastCalculationTick
                && waypoints.equals(p.waypoints);
    }

    @Override
    public int hashCode() {
        int result = waypoints.hashCode();
        result = 31 * result + currentIndex;
        result = 31 * result + targetEntityId;
        result = 31 * result + Long.hashCode(lastCalculationTick);
        return result;
    }

    @Override
    public String toString() {
        return "Path[waypoints=" + waypoints.size() + ", currentIndex=" + currentIndex
                + ", targetEntityId=" + targetEntityId + "]";
    }
}
