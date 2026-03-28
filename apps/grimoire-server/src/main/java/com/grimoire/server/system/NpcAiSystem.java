package com.grimoire.server.system;
import io.micronaut.core.annotation.Order;

import com.grimoire.server.component.*;
import com.grimoire.server.config.GameConfig;
import com.grimoire.ecs.EcsWorld;
import com.grimoire.ecs.GameSystem;
import com.grimoire.ecs.SpatialGrid;
import com.grimoire.server.navigation.AStarPathfinder;
import com.grimoire.server.navigation.NavigationGrid;
import com.grimoire.server.navigation.NavigationSystem;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Manages NPC AI behavior.
 * 
 * <p>Uses spatial partitioning via {@link SpatialGrid} for efficient player detection,
 * reducing complexity from O(N×M) to O(N×k) where k is the average nearby entities.</p>
 * 
 * <p>Implements pathfinding using A* algorithm via {@link NavigationSystem} for intelligent
 * obstacle avoidance. NPCs will find paths around obstacles rather than moving in direct lines.</p>
 * 
 * <p>Implements a leash mechanic via {@link SpawnPoint} component to prevent infinite kiting.
 * When an NPC moves beyond its leash radius, it will stop pursuing targets and return
 * to its spawn point.</p>
 */
@Order(300)
@Singleton
@Slf4j
public class NpcAiSystem implements GameSystem {
    
    private static final double WAYPOINT_TOLERANCE = 5.0; // Distance at which waypoint is considered reached
    private static final int PATH_RECALCULATION_INTERVAL = 40; // Recalculate path every 40 ticks (2 seconds at 20 TPS)
    private static final double TARGET_MOVED_THRESHOLD = 32.0; // Repath if target moved more than this distance
    private static final int MAX_PATH_CALCULATIONS_PER_TICK = 5; // Limit pathfinding per tick to prevent lag
    
    private final EcsWorld ecsWorld;
    private final SpatialGridSystem spatialGridSystem;
    private final NavigationSystem navigationSystem;
    private final Random random = new Random();
    private final double aggroRange;
    private final double npcSpeed;
    private final double defaultLeashRadius;
    private int pathCalculationsThisTick = 0;
    
    public NpcAiSystem(EcsWorld ecsWorld, SpatialGridSystem spatialGridSystem, GameConfig gameConfig) {
        this(ecsWorld, spatialGridSystem, null, gameConfig);
    }
    
    public NpcAiSystem(EcsWorld ecsWorld, SpatialGridSystem spatialGridSystem, 
                       NavigationSystem navigationSystem, GameConfig gameConfig) {
        this.ecsWorld = ecsWorld;
        this.spatialGridSystem = spatialGridSystem;
        this.navigationSystem = navigationSystem;
        this.aggroRange = gameConfig.getNpcAggroRange();
        this.npcSpeed = gameConfig.getNpcSpeed();
        this.defaultLeashRadius = gameConfig.getNpcLeashRadius();
    }
    
    @Override
    public void tick(float deltaTime) {
        pathCalculationsThisTick = 0;
        
        for (String entityId : ecsWorld.getEntitiesWithComponent(NpcAi.class)) {
            ecsWorld.getComponent(entityId, NpcAi.class).ifPresent(ai -> {
                if (ai.type() == NpcAi.AiType.FRIENDLY_WANDER) {
                    handleFriendlyWander(entityId);
                } else if (ai.type() == NpcAi.AiType.HOSTILE_AGGRO_MELEE) {
                    handleHostileAggro(entityId);
                }
            });
        }
    }
    
    private void handleFriendlyWander(String entityId) {
        // Random wandering behavior
        if (random.nextDouble() < 0.05) { // 5% chance per tick to change direction
            double vx = (random.nextDouble() - 0.5) * 2.0; // -1 to 1
            double vy = (random.nextDouble() - 0.5) * 2.0; // -1 to 1
            ecsWorld.addComponent(entityId, new Velocity(vx, vy));
        }
    }
    
    private void handleHostileAggro(String entityId) {
        var npcZone = ecsWorld.getComponent(entityId, Zone.class);
        var npcPos = ecsWorld.getComponent(entityId, Position.class);
        
        if (npcZone.isEmpty() || npcPos.isEmpty()) {
            return;
        }
        
        Position pos = npcPos.get();
        String zoneId = npcZone.get().zoneId();
        
        // Check leash distance - if too far from spawn, return home
        if (isOutOfLeashRange(entityId, pos)) {
            returnToSpawnPoint(entityId, pos, zoneId);
            return;
        }
        
        // Use spatial grid to get only nearby entities instead of all players
        SpatialGrid grid = spatialGridSystem.getGrid();
        Set<String> nearbyEntities = grid.getNearbyEntities(pos.x(), pos.y(), zoneId);
        
        String closestPlayerId = null;
        double closestDistance = Double.MAX_VALUE;
        Position closestPlayerPos = null;
        
        // Filter nearby entities for players only
        for (String nearbyId : nearbyEntities) {
            // Skip if not a player (doesn't have PlayerConnection component)
            if (!ecsWorld.hasComponent(nearbyId, PlayerConnection.class)) {
                continue;
            }
            
            var playerPos = ecsWorld.getComponent(nearbyId, Position.class);
            
            if (playerPos.isPresent()) {
                double dx = playerPos.get().x() - pos.x();
                double dy = playerPos.get().y() - pos.y();
                double distance = Math.sqrt(dx * dx + dy * dy);
                
                if (distance < closestDistance && distance <= aggroRange) {
                    closestDistance = distance;
                    closestPlayerId = nearbyId;
                    closestPlayerPos = playerPos.get();
                }
            }
        }
        
        // Chase the closest player using pathfinding
        if (closestPlayerId != null && closestPlayerPos != null) {
            if (navigationSystem != null) {
                handlePathfindingChase(entityId, pos, closestPlayerId, closestPlayerPos, zoneId);
            } else {
                // Fallback to direct movement if no navigation system
                handleDirectChase(entityId, pos, closestPlayerPos);
            }
        } else {
            // No player in range, stop moving and clear path
            ecsWorld.addComponent(entityId, new Velocity(0, 0));
            ecsWorld.removeComponent(entityId, Path.class);
        }
    }
    
    /**
     * Handles pathfinding-based chase behavior.
     */
    private void handlePathfindingChase(String entityId, Position npcPos, 
                                         String targetId, Position targetPos, String zoneId) {
        var existingPath = ecsWorld.getComponent(entityId, Path.class);
        long currentTick = ecsWorld.getCurrentTick();
        
        boolean needsRepath = needsRepathing(existingPath, targetId, targetPos, currentTick);
        
        if (needsRepath && pathCalculationsThisTick < MAX_PATH_CALCULATIONS_PER_TICK) {
            calculateNewPath(entityId, npcPos, targetId, targetPos, zoneId, currentTick);
            pathCalculationsThisTick++;
        }
        
        // Follow the path
        followPath(entityId, npcPos);
    }
    
    /**
     * Determines if the NPC needs to recalculate its path.
     */
    private boolean needsRepathing(java.util.Optional<Path> existingPath, 
                                   String targetId, Position targetPos, long currentTick) {
        if (existingPath.isEmpty()) {
            return true;
        }
        
        Path path = existingPath.get();
        
        // Path is empty (finished)
        if (path.isEmpty()) {
            return true;
        }
        
        // Different target
        if (path.targetEntityId() == null || !path.targetEntityId().equals(targetId)) {
            return true;
        }
        
        // Path is old - recalculate periodically
        if (currentTick - path.lastCalculationTick() > PATH_RECALCULATION_INTERVAL) {
            return true;
        }
        
        // Check if target has moved significantly
        Position lastWaypoint = path.getLastWaypoint();
        if (lastWaypoint != null) {
            double dx = targetPos.x() - lastWaypoint.x();
            double dy = targetPos.y() - lastWaypoint.y();
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance > TARGET_MOVED_THRESHOLD) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Calculates a new path to the target.
     */
    private void calculateNewPath(String entityId, Position npcPos, 
                                  String targetId, Position targetPos, 
                                  String zoneId, long currentTick) {
        NavigationGrid navGrid = navigationSystem.getOrCreateGrid(zoneId);
        
        List<Position> waypoints = AStarPathfinder.findPath(
                npcPos.x(), npcPos.y(),
                targetPos.x(), targetPos.y(),
                navGrid);
        
        if (waypoints != null && !waypoints.isEmpty()) {
            // Apply path smoothing for more natural movement
            waypoints = AStarPathfinder.smoothPath(waypoints, navGrid);
            
            // Skip the first waypoint if it's very close (our current position)
            if (waypoints.size() > 1) {
                Position first = waypoints.get(0);
                double dx = first.x() - npcPos.x();
                double dy = first.y() - npcPos.y();
                if (Math.sqrt(dx * dx + dy * dy) < WAYPOINT_TOLERANCE) {
                    waypoints = waypoints.subList(1, waypoints.size());
                }
            }
            
            Path newPath = Path.fromList(waypoints, targetId, currentTick);
            ecsWorld.addComponent(entityId, newPath);
            log.debug("NPC {} calculated path with {} waypoints to target {}", 
                    entityId, waypoints.size(), targetId);
        } else {
            // No path found - fall back to direct movement
            log.debug("NPC {} could not find path to target {}, using direct movement", entityId, targetId);
            ecsWorld.removeComponent(entityId, Path.class);
            handleDirectChase(entityId, npcPos, targetPos);
        }
    }
    
    /**
     * Follows the current path by moving towards the next waypoint.
     */
    private void followPath(String entityId, Position npcPos) {
        var pathOpt = ecsWorld.getComponent(entityId, Path.class);
        
        if (pathOpt.isEmpty() || pathOpt.get().isEmpty()) {
            ecsWorld.addComponent(entityId, new Velocity(0, 0));
            return;
        }
        
        Path path = pathOpt.get();
        Position currentWaypoint = path.getCurrentWaypoint();
        
        if (currentWaypoint == null) {
            ecsWorld.addComponent(entityId, new Velocity(0, 0));
            return;
        }
        
        double dx = currentWaypoint.x() - npcPos.x();
        double dy = currentWaypoint.y() - npcPos.y();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Check if we've reached the waypoint
        if (distance < WAYPOINT_TOLERANCE) {
            // Advance to next waypoint (immutable operation creates new Path)
            Path advancedPath = path.advanceToNextWaypoint();
            ecsWorld.addComponent(entityId, advancedPath);
            
            // Get new current waypoint
            currentWaypoint = advancedPath.getCurrentWaypoint();
            if (currentWaypoint == null) {
                // Path complete
                ecsWorld.addComponent(entityId, new Velocity(0, 0));
                return;
            }
            
            dx = currentWaypoint.x() - npcPos.x();
            dy = currentWaypoint.y() - npcPos.y();
            distance = Math.sqrt(dx * dx + dy * dy);
        }
        
        if (distance > 0) {
            double vx = (dx / distance) * npcSpeed;
            double vy = (dy / distance) * npcSpeed;
            ecsWorld.addComponent(entityId, new Velocity(vx, vy));
        }
    }
    
    /**
     * Handles direct chase without pathfinding (fallback behavior).
     */
    private void handleDirectChase(String entityId, Position npcPos, Position targetPos) {
        double dx = targetPos.x() - npcPos.x();
        double dy = targetPos.y() - npcPos.y();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0) {
            double vx = (dx / distance) * npcSpeed;
            double vy = (dy / distance) * npcSpeed;
            ecsWorld.addComponent(entityId, new Velocity(vx, vy));
        }
    }
    
    /**
     * Checks if the NPC is outside its leash range from spawn point.
     * 
     * @param entityId the NPC entity ID
     * @param currentPos the NPC's current position
     * @return true if outside leash range, false otherwise
     */
    private boolean isOutOfLeashRange(String entityId, Position currentPos) {
        var spawnPointOpt = ecsWorld.getComponent(entityId, SpawnPoint.class);
        if (spawnPointOpt.isEmpty()) {
            return false; // No spawn point means no leash
        }
        
        SpawnPoint spawn = spawnPointOpt.get();
        double dx = currentPos.x() - spawn.x();
        double dy = currentPos.y() - spawn.y();
        double distanceSquared = dx * dx + dy * dy;
        double leashRadius = spawn.leashRadius() > 0 ? spawn.leashRadius() : defaultLeashRadius;
        
        return distanceSquared > leashRadius * leashRadius;
    }
    
    /**
     * Directs the NPC to return to its spawn point, using pathfinding if available.
     * 
     * @param entityId the NPC entity ID
     * @param currentPos the NPC's current position
     * @param zoneId the zone ID for pathfinding
     */
    private void returnToSpawnPoint(String entityId, Position currentPos, String zoneId) {
        var spawnPointOpt = ecsWorld.getComponent(entityId, SpawnPoint.class);
        if (spawnPointOpt.isEmpty()) {
            ecsWorld.addComponent(entityId, new Velocity(0, 0));
            return;
        }
        
        // Clear any existing target path
        ecsWorld.removeComponent(entityId, Path.class);
        
        SpawnPoint spawn = spawnPointOpt.get();
        double dx = spawn.x() - currentPos.x();
        double dy = spawn.y() - currentPos.y();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 1.0) {
            // Close enough to spawn, stop moving
            ecsWorld.addComponent(entityId, new Velocity(0, 0));
            
            // Optionally restore HP while resetting
            ecsWorld.getComponent(entityId, Stats.class).ifPresent(stats -> {
                if (stats.hp() < stats.maxHp()) {
                    ecsWorld.addComponent(entityId, new Stats(stats.maxHp(), stats.maxHp(), stats.defense(), stats.attack()));
                    log.debug("NPC {} returned to spawn and restored to full HP", entityId);
                }
            });
        } else {
            // Move towards spawn point at increased speed
            double returnSpeed = npcSpeed * 1.5; // Move faster when returning
            double vx = (dx / distance) * returnSpeed;
            double vy = (dy / distance) * returnSpeed;
            ecsWorld.addComponent(entityId, new Velocity(vx, vy));
        }
    }
}
