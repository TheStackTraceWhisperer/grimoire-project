package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.GameSystem;
import com.grimoire.application.core.port.GameConfig;
import com.grimoire.domain.combat.component.AttackIntent;
import com.grimoire.domain.combat.component.NpcAi;
import com.grimoire.domain.core.component.Dead;
import com.grimoire.domain.core.component.PlayerControlled;
import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.SpawnPoint;
import com.grimoire.domain.core.component.Velocity;
import com.grimoire.domain.core.component.Zone;
import com.grimoire.domain.navigation.spatial.SpatialGrid;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * Manages NPC AI behaviour: wandering and hostile aggro.
 *
 * <p>
 * {@link NpcAi.AiType#FRIENDLY_WANDER FRIENDLY_WANDER} NPCs randomly change
 * velocity. {@link NpcAi.AiType#HOSTILE_AGGRO_MELEE HOSTILE_AGGRO_MELEE} NPCs
 * chase the nearest {@link PlayerControlled} entity within aggro range,
 * attacking when within melee range. A leash mechanic via {@link SpawnPoint}
 * prevents infinite kiting.
 * </p>
 */
public class NpcAiSystem implements GameSystem {

    /** Probability per tick that a wandering NPC changes direction. */
    private static final double WANDER_CHANCE = 0.05;

    /** Minimum distance threshold to avoid zero-distance division. */
    private static final double MIN_DISTANCE = 0.01;

    /** The ECS world. */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "EcsWorld is a managed collaborator, not external mutable data")
    private final EcsWorld ecsWorld;

    /** Spatial grid system for proximity queries. */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "SpatialGridSystem is a managed collaborator")
    private final SpatialGridSystem spatialGridSystem;

    /** Random source for wandering behaviour. */
    private final Random random;

    /** Aggro detection range (squared comparisons avoid sqrt). */
    private final double aggroRange;

    /** NPC movement speed in world units per second. */
    private final double npcSpeed;

    /** Maximum attack range in world units. */
    private final double attackRange;

    /** Default leash radius when spawn-point has no override. */
    private final double defaultLeashRadius;

    /**
     * Creates an NPC AI system.
     *
     * @param ecsWorld
     *            the ECS world
     * @param gameConfig
     *            game tuning parameters
     * @param spatialGridSystem
     *            spatial grid for proximity queries
     * @param random
     *            random source (inject a seeded instance for tests)
     */
    public NpcAiSystem(EcsWorld ecsWorld, GameConfig gameConfig,
            SpatialGridSystem spatialGridSystem, Random random) {
        this.ecsWorld = Objects.requireNonNull(ecsWorld, "ecsWorld must not be null");
        Objects.requireNonNull(gameConfig, "gameConfig must not be null");
        this.spatialGridSystem = Objects.requireNonNull(spatialGridSystem,
                "spatialGridSystem must not be null");
        this.random = Objects.requireNonNull(random, "random must not be null");
        this.aggroRange = gameConfig.npcAggroRange();
        this.npcSpeed = gameConfig.npcSpeed();
        this.attackRange = gameConfig.attackRange();
        this.defaultLeashRadius = gameConfig.npcLeashRadius();
    }

    @Override
    public void tick(float deltaTime) {
        for (String entityId : ecsWorld.getEntitiesWithComponent(NpcAi.class)) {
            if (ecsWorld.hasComponent(entityId, Dead.class)) {
                continue;
            }
            ecsWorld.getComponent(entityId, NpcAi.class).ifPresent(ai -> {
                if (ai.type() == NpcAi.AiType.FRIENDLY_WANDER) {
                    handleFriendlyWander(entityId);
                } else if (ai.type() == NpcAi.AiType.HOSTILE_AGGRO_MELEE) {
                    handleHostileAggro(entityId);
                }
            });
        }
    }

    /**
     * Randomly sets velocity for a wandering NPC.
     *
     * @param entityId
     *            the NPC entity
     */
    private void handleFriendlyWander(String entityId) {
        if (random.nextDouble() < WANDER_CHANCE) {
            double vx = (random.nextDouble() - 0.5) * 2.0 * npcSpeed;
            double vy = (random.nextDouble() - 0.5) * 2.0 * npcSpeed;
            ecsWorld.addComponent(entityId, new Velocity(vx, vy));
        }
    }

    /**
     * Finds the nearest player within aggro range and chases or attacks them.
     * Returns to spawn if beyond leash radius.
     *
     * @param entityId
     *            the hostile NPC entity
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.CognitiveComplexity"})
    private void handleHostileAggro(String entityId) {
        Optional<Zone> zoneOpt = ecsWorld.getComponent(entityId, Zone.class);
        Optional<Position> posOpt = ecsWorld.getComponent(entityId, Position.class);
        if (zoneOpt.isEmpty() || posOpt.isEmpty()) {
            return;
        }

        Position npcPos = posOpt.get();
        String zoneId = zoneOpt.get().zoneId();

        if (isOutOfLeashRange(entityId, npcPos)) {
            returnToSpawnPoint(entityId, npcPos);
            return;
        }

        SpatialGrid grid = spatialGridSystem.getGrid();
        Set<String> nearbyEntities = grid.getNearbyEntities(npcPos.x(), npcPos.y(), zoneId);

        String closestPlayerId = null;
        double closestDistSq = Double.MAX_VALUE;
        Position closestPlayerPos = null;

        for (String nearbyId : nearbyEntities) {
            if (!ecsWorld.hasComponent(nearbyId, PlayerControlled.class)) {
                continue;
            }
            if (ecsWorld.hasComponent(nearbyId, Dead.class)) {
                continue;
            }

            Optional<Position> playerPosOpt = ecsWorld.getComponent(nearbyId, Position.class);
            if (playerPosOpt.isPresent()) {
                double dx = playerPosOpt.get().x() - npcPos.x();
                double dy = playerPosOpt.get().y() - npcPos.y();
                double distSq = dx * dx + dy * dy;
                if (distSq < closestDistSq && distSq <= aggroRange * aggroRange) {
                    closestDistSq = distSq;
                    closestPlayerId = nearbyId;
                    closestPlayerPos = playerPosOpt.get();
                }
            }
        }

        if (closestPlayerId != null) {
            double distance = Math.sqrt(closestDistSq);
            if (distance <= attackRange) {
                ecsWorld.addComponent(entityId, new AttackIntent(closestPlayerId));
                ecsWorld.addComponent(entityId, new Velocity(0, 0));
            } else {
                moveToward(entityId, npcPos, closestPlayerPos);
            }
        } else {
            ecsWorld.addComponent(entityId, new Velocity(0, 0));
        }
    }

    /**
     * Checks whether the NPC is beyond its leash radius from its spawn point.
     *
     * @param entityId
     *            the NPC entity
     * @param currentPos
     *            the NPC's current position
     * @return {@code true} if beyond leash range
     */
    private boolean isOutOfLeashRange(String entityId, Position currentPos) {
        Optional<SpawnPoint> spawnOpt = ecsWorld.getComponent(entityId, SpawnPoint.class);
        if (spawnOpt.isEmpty()) {
            return false;
        }
        SpawnPoint spawn = spawnOpt.get();
        double leash = spawn.leashRadius() > 0 ? spawn.leashRadius() : defaultLeashRadius;
        double dx = currentPos.x() - spawn.x();
        double dy = currentPos.y() - spawn.y();
        return dx * dx + dy * dy > leash * leash;
    }

    /**
     * Sets velocity toward the NPC's spawn point.
     *
     * @param entityId
     *            the NPC entity
     * @param currentPos
     *            the NPC's current position
     */
    private void returnToSpawnPoint(String entityId, Position currentPos) {
        Optional<SpawnPoint> spawnOpt = ecsWorld.getComponent(entityId, SpawnPoint.class);
        if (spawnOpt.isEmpty()) {
            ecsWorld.addComponent(entityId, new Velocity(0, 0));
            return;
        }
        SpawnPoint spawn = spawnOpt.get();
        moveToward(entityId, currentPos, new Position(spawn.x(), spawn.y()));
    }

    /**
     * Sets the entity's velocity to move toward a target position at NPC speed.
     *
     * @param entityId
     *            the entity to move
     * @param from
     *            the current position
     * @param to
     *            the target position
     */
    private void moveToward(String entityId, Position from, Position to) {
        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < MIN_DISTANCE) {
            ecsWorld.addComponent(entityId, new Velocity(0, 0));
            return;
        }
        double vx = dx / distance * npcSpeed;
        double vy = dy / distance * npcSpeed;
        ecsWorld.addComponent(entityId, new Velocity(vx, vy));
    }
}
