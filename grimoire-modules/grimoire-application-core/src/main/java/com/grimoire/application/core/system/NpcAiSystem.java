package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.ComponentManager;
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

import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Manages NPC AI behaviour: wandering and hostile aggro.
 *
 * <p>
 * Iterates all entities using a contiguous for-loop over the NpcAi array.
 * </p>
 */
public class NpcAiSystem implements GameSystem {

    private static final double WANDER_CHANCE = 0.05;
    private static final double MIN_DISTANCE = 0.01;

    private final EcsWorld ecsWorld;

    private final SpatialGridSystem spatialGridSystem;

    private final Random random;
    private final double aggroRange;
    private final double npcSpeed;
    private final double attackRange;
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
     *            random source
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
        int max = ecsWorld.getMaxEntityId();
        boolean[] alive = ecsWorld.getAlive();
        ComponentManager cm = ecsWorld.getComponentManager();
        NpcAi[] npcAis = cm.getNpcAis();
        Dead[] deads = cm.getDeads();

        for (int i = 0; i < max; i++) {
            if (!alive[i] || npcAis[i] == null || deads[i] != null) {
                continue;
            }
            NpcAi ai = npcAis[i];
            if (ai.type == NpcAi.AiType.FRIENDLY_WANDER) {
                handleFriendlyWander(i, cm);
            } else if (ai.type == NpcAi.AiType.HOSTILE_AGGRO_MELEE) {
                handleHostileAggro(i, cm);
            }
        }
    }

    private void handleFriendlyWander(int entityId, ComponentManager cm) {
        if (random.nextDouble() < WANDER_CHANCE) {
            double vx = (random.nextDouble() - 0.5) * 2.0 * npcSpeed;
            double vy = (random.nextDouble() - 0.5) * 2.0 * npcSpeed;
            Velocity vel = cm.getVelocities()[entityId];
            if (vel == null) {
                cm.addComponent(entityId, new Velocity(vx, vy));
            } else {
                vel.update(vx, vy);
            }
        }
    }

    private void handleHostileAggro(int entityId, ComponentManager cm) {
        Zone zone = cm.getZones()[entityId];
        Position npcPos = cm.getPositions()[entityId];
        if (zone == null || npcPos == null) {
            return;
        }

        String zoneId = zone.zoneId;

        if (isOutOfLeashRange(entityId, npcPos, cm)) {
            returnToSpawnPoint(entityId, npcPos, cm);
            return;
        }

        SpatialGrid grid = spatialGridSystem.getGrid();
        Set<Integer> nearbyEntities = grid.getNearbyEntities(npcPos.x, npcPos.y, zoneId);

        int closestPlayerId = -1;
        double closestDistSq = Double.MAX_VALUE;
        Position closestPlayerPos = null;

        PlayerControlled[] pcs = cm.getPlayerControlled();
        Dead[] deads = cm.getDeads();
        Position[] positions = cm.getPositions();

        for (int nearbyId : nearbyEntities) {
            if (pcs[nearbyId] == null || deads[nearbyId] != null) {
                continue;
            }
            Position playerPos = positions[nearbyId];
            if (playerPos != null) {
                double dx = playerPos.x - npcPos.x;
                double dy = playerPos.y - npcPos.y;
                double distSq = dx * dx + dy * dy;
                if (distSq < closestDistSq && distSq <= aggroRange * aggroRange) {
                    closestDistSq = distSq;
                    closestPlayerId = nearbyId;
                    closestPlayerPos = playerPos;
                }
            }
        }

        if (closestPlayerId >= 0) {
            double distance = Math.sqrt(closestDistSq);
            if (distance <= attackRange) {
                cm.getAttackIntents()[entityId] = new AttackIntent(closestPlayerId);
                Velocity vel = cm.getVelocities()[entityId];
                if (vel == null) {
                    cm.addComponent(entityId, new Velocity(0, 0));
                } else {
                    vel.update(0, 0);
                }
            } else {
                moveToward(entityId, npcPos, closestPlayerPos, cm);
            }
        } else {
            Velocity vel = cm.getVelocities()[entityId];
            if (vel == null) {
                cm.addComponent(entityId, new Velocity(0, 0));
            } else {
                vel.update(0, 0);
            }
        }
    }

    private boolean isOutOfLeashRange(int entityId, Position currentPos,
            ComponentManager cm) {
        SpawnPoint spawn = cm.getSpawnPoints()[entityId];
        if (spawn == null) {
            return false;
        }
        double leash = spawn.leashRadius > 0 ? spawn.leashRadius : defaultLeashRadius;
        double dx = currentPos.x - spawn.x;
        double dy = currentPos.y - spawn.y;
        return dx * dx + dy * dy > leash * leash;
    }

    private void returnToSpawnPoint(int entityId, Position currentPos,
            ComponentManager cm) {
        SpawnPoint spawn = cm.getSpawnPoints()[entityId];
        if (spawn == null) {
            Velocity vel = cm.getVelocities()[entityId];
            if (vel == null) {
                cm.addComponent(entityId, new Velocity(0, 0));
            } else {
                vel.update(0, 0);
            }
            return;
        }
        moveToward(entityId, currentPos, new Position(spawn.x, spawn.y), cm);
    }

    private void moveToward(int entityId, Position from, Position to,
            ComponentManager cm) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < MIN_DISTANCE) {
            Velocity vel = cm.getVelocities()[entityId];
            if (vel == null) {
                cm.addComponent(entityId, new Velocity(0, 0));
            } else {
                vel.update(0, 0);
            }
            return;
        }
        double vx = dx / distance * npcSpeed;
        double vy = dy / distance * npcSpeed;
        Velocity vel = cm.getVelocities()[entityId];
        if (vel == null) {
            cm.addComponent(entityId, new Velocity(vx, vy));
        } else {
            vel.update(vx, vy);
        }
    }
}
