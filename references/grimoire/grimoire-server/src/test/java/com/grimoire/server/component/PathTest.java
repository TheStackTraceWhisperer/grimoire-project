package com.grimoire.server.component;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PathTest {
    
    @Test
    void testPathCreation() {
        List<Position> waypoints = new ArrayList<>();
        waypoints.add(new Position(100, 100));
        waypoints.add(new Position(200, 200));
        
        Path path = new Path(waypoints, 0, "target-entity", 100L);
        
        assertNotNull(path.waypoints());
        assertEquals(0, path.currentIndex());
        assertEquals("target-entity", path.targetEntityId());
        assertEquals(100L, path.lastCalculationTick());
    }
    
    @Test
    void testPathIsComponent() {
        Path path = new Path(List.of(), 0, null, 0L);
        assertInstanceOf(Component.class, path);
    }
    
    @Test
    void testFromListWithTargetEntity() {
        List<Position> waypointList = List.of(
            new Position(100, 100),
            new Position(200, 200),
            new Position(300, 300)
        );
        
        Path path = Path.fromList(waypointList, "target-id", 50L);
        
        assertEquals(3, path.size());
        assertEquals("target-id", path.targetEntityId());
        assertEquals(50L, path.lastCalculationTick());
        assertEquals(0, path.currentIndex());
    }
    
    @Test
    void testFromListWithoutTargetEntity() {
        List<Position> waypointList = List.of(
            new Position(100, 100),
            new Position(200, 200)
        );
        
        Path path = Path.fromList(waypointList, 75L);
        
        assertEquals(2, path.size());
        assertNull(path.targetEntityId());
        assertEquals(75L, path.lastCalculationTick());
    }
    
    @Test
    void testIsEmpty() {
        Path emptyPath = new Path(List.of(), 0, null, 0L);
        assertTrue(emptyPath.isEmpty());
        
        Path nullPath = new Path(null, 0, null, 0L);
        assertTrue(nullPath.isEmpty());
        
        List<Position> waypoints = List.of(new Position(100, 100));
        Path nonEmptyPath = new Path(waypoints, 0, null, 0L);
        assertFalse(nonEmptyPath.isEmpty());
        
        // Path is empty when index >= size
        Path completedPath = new Path(waypoints, 1, null, 0L);
        assertTrue(completedPath.isEmpty());
    }
    
    @Test
    void testGetCurrentWaypoint() {
        List<Position> waypointList = List.of(
            new Position(100, 100),
            new Position(200, 200)
        );
        Path path = Path.fromList(waypointList, 0L);
        
        Position first = path.getCurrentWaypoint();
        
        assertEquals(100, first.x());
        assertEquals(100, first.y());
        assertEquals(2, path.size(), "Getting current waypoint should not modify path");
    }
    
    @Test
    void testGetCurrentWaypointOnEmptyPath() {
        Path emptyPath = new Path(List.of(), 0, null, 0L);
        assertNull(emptyPath.getCurrentWaypoint());
        
        Path nullPath = new Path(null, 0, null, 0L);
        assertNull(nullPath.getCurrentWaypoint());
    }
    
    @Test
    void testAdvanceToNextWaypoint() {
        List<Position> waypointList = List.of(
            new Position(100, 100),
            new Position(200, 200),
            new Position(300, 300)
        );
        Path path = Path.fromList(waypointList, 0L);
        
        assertEquals(100, path.getCurrentWaypoint().x());
        assertEquals(3, path.remainingWaypoints());
        
        // Advance to second waypoint
        Path path2 = path.advanceToNextWaypoint();
        assertEquals(200, path2.getCurrentWaypoint().x());
        assertEquals(2, path2.remainingWaypoints());
        
        // Original path is unchanged (immutable)
        assertEquals(100, path.getCurrentWaypoint().x());
        assertEquals(3, path.remainingWaypoints());
        
        // Advance to third waypoint
        Path path3 = path2.advanceToNextWaypoint();
        assertEquals(300, path3.getCurrentWaypoint().x());
        assertEquals(1, path3.remainingWaypoints());
        
        // Advance past end
        Path path4 = path3.advanceToNextWaypoint();
        assertTrue(path4.isEmpty());
        assertNull(path4.getCurrentWaypoint());
    }
    
    @Test
    void testRemainingWaypoints() {
        List<Position> waypointList = List.of(
            new Position(100, 100),
            new Position(200, 200),
            new Position(300, 300),
            new Position(400, 400)
        );
        Path path = Path.fromList(waypointList, 0L);
        
        assertEquals(4, path.remainingWaypoints());
        
        path = path.advanceToNextWaypoint();
        assertEquals(3, path.remainingWaypoints());
        
        path = path.advanceToNextWaypoint();
        assertEquals(2, path.remainingWaypoints());
        
        Path emptyPath = new Path(List.of(), 0, null, 0L);
        assertEquals(0, emptyPath.remainingWaypoints());
        
        Path nullPath = new Path(null, 0, null, 0L);
        assertEquals(0, nullPath.remainingWaypoints());
    }
    
    @Test
    void testSize() {
        List<Position> waypointList = List.of(
            new Position(100, 100),
            new Position(200, 200),
            new Position(300, 300),
            new Position(400, 400)
        );
        Path path = Path.fromList(waypointList, 0L);
        
        assertEquals(4, path.size());
        
        // Size doesn't change when advancing
        path = path.advanceToNextWaypoint();
        assertEquals(4, path.size());
        
        Path emptyPath = new Path(List.of(), 0, null, 0L);
        assertEquals(0, emptyPath.size());
        
        Path nullPath = new Path(null, 0, null, 0L);
        assertEquals(0, nullPath.size());
    }
    
    @Test
    void testGetLastWaypoint() {
        List<Position> waypointList = List.of(
            new Position(100, 100),
            new Position(200, 200),
            new Position(300, 300)
        );
        Path path = Path.fromList(waypointList, 0L);
        
        Position last = path.getLastWaypoint();
        assertEquals(300, last.x());
        assertEquals(300, last.y());
        
        Path emptyPath = new Path(List.of(), 0, null, 0L);
        assertNull(emptyPath.getLastWaypoint());
        
        Path nullPath = new Path(null, 0, null, 0L);
        assertNull(nullPath.getLastWaypoint());
    }
}
