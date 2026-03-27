package com.grimoire.server.component;

import java.util.Collections;
import java.util.List;

/**
 * Component storing an entity's current navigation path.
 * 
 * <p>This component is used by the AI system to follow a pre-calculated path
 * to a target location or entity. The path consists of waypoints that the
 * entity will move towards sequentially.</p>
 * 
 * <p>The component is immutable - to advance to the next waypoint, a new Path
 * component with an incremented index should be created and added to the entity.</p>
 * 
 * @param waypoints the immutable list of positions to follow
 * @param currentIndex the index of the current waypoint being followed
 * @param targetEntityId the ID of the entity being followed, or null if following a fixed path
 * @param lastCalculationTick the game tick when this path was calculated
 */
public record Path(
        List<Position> waypoints,
        int currentIndex,
        String targetEntityId,
        long lastCalculationTick
) implements Component {
    
    /**
     * Creates a Path component with a list of waypoints.
     * 
     * @param waypointList the list of waypoints
     * @param targetEntityId the target entity ID
     * @param lastCalculationTick the tick when the path was calculated
     * @return a new Path component
     */
    public static Path fromList(List<Position> waypointList, String targetEntityId, long lastCalculationTick) {
        return new Path(List.copyOf(waypointList), 0, targetEntityId, lastCalculationTick);
    }
    
    /**
     * Creates a Path with no target entity.
     * 
     * @param waypointList the list of waypoints
     * @param lastCalculationTick the tick when the path was calculated
     * @return a new Path component
     */
    public static Path fromList(List<Position> waypointList, long lastCalculationTick) {
        return fromList(waypointList, null, lastCalculationTick);
    }
    
    /**
     * Checks if the path is empty or completed.
     * 
     * @return true if there are no more waypoints to follow
     */
    public boolean isEmpty() {
        return waypoints == null || currentIndex >= waypoints.size();
    }
    
    /**
     * Gets the current waypoint being followed.
     * 
     * @return the current waypoint, or null if the path is empty
     */
    public Position getCurrentWaypoint() {
        if (waypoints == null || currentIndex >= waypoints.size()) {
            return null;
        }
        return waypoints.get(currentIndex);
    }
    
    /**
     * Creates a new Path with the index advanced to the next waypoint.
     * 
     * @return a new Path component with incremented index
     */
    public Path advanceToNextWaypoint() {
        return new Path(waypoints, currentIndex + 1, targetEntityId, lastCalculationTick);
    }
    
    /**
     * Gets the number of remaining waypoints (including current).
     * 
     * @return the number of waypoints left to follow
     */
    public int remainingWaypoints() {
        if (waypoints == null) {
            return 0;
        }
        return Math.max(0, waypoints.size() - currentIndex);
    }
    
    /**
     * Gets the total number of waypoints in the path.
     * 
     * @return the total number of waypoints
     */
    public int size() {
        return waypoints != null ? waypoints.size() : 0;
    }
    
    /**
     * Gets the last waypoint in the path.
     * 
     * @return the last waypoint, or null if the path is empty
     */
    public Position getLastWaypoint() {
        if (waypoints == null || waypoints.isEmpty()) {
            return null;
        }
        return waypoints.get(waypoints.size() - 1);
    }
}
