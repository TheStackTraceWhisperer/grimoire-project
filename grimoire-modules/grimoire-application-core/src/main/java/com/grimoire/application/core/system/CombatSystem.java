package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.ComponentManager;
import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.GameSystem;
import com.grimoire.application.core.port.GameConfig;
import com.grimoire.application.core.port.GameEventPort;
import com.grimoire.domain.combat.component.AttackCooldown;
import com.grimoire.domain.combat.component.AttackIntent;
import com.grimoire.domain.combat.component.Monster;
import com.grimoire.domain.combat.rule.CombatRules;
import com.grimoire.domain.combat.rule.LevelingRules;
import com.grimoire.domain.core.component.Dead;
import com.grimoire.domain.core.component.Dirty;
import com.grimoire.domain.core.component.Experience;
import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.Stats;
import com.grimoire.domain.core.component.Zone;

import java.util.Objects;

/**
 * Processes combat logic: cooldowns, attack resolution, death, and XP rewards.
 *
 * <p>
 * Iterates entities using contiguous for-loops over component arrays.
 * </p>
 */
public class CombatSystem implements GameSystem {

    private static final System.Logger LOG = System.getLogger(CombatSystem.class.getName());

    private final EcsWorld ecsWorld;

    private final GameEventPort gameEventPort;

    private final SpatialGridSystem spatialGridSystem;

    private final double attackRange;
    private final int attackCooldownTicks;

    /**
     * Creates a combat system.
     *
     * @param ecsWorld
     *            the ECS world
     * @param gameConfig
     *            configuration
     * @param gameEventPort
     *            port for entity despawn notifications
     * @param spatialGridSystem
     *            spatial grid for entity cleanup on death
     */
    public CombatSystem(EcsWorld ecsWorld, GameConfig gameConfig,
            GameEventPort gameEventPort, SpatialGridSystem spatialGridSystem) {
        this.ecsWorld = Objects.requireNonNull(ecsWorld, "ecsWorld must not be null");
        Objects.requireNonNull(gameConfig, "gameConfig must not be null");
        this.gameEventPort = Objects.requireNonNull(gameEventPort,
                "gameEventPort must not be null");
        this.spatialGridSystem = Objects.requireNonNull(spatialGridSystem,
                "spatialGridSystem must not be null");
        this.attackRange = gameConfig.attackRange();
        this.attackCooldownTicks = gameConfig.attackCooldownTicks();
    }

    @Override
    public void tick(float deltaTime) {
        processCooldowns();
        processAttacks();
        processDeath();
    }

    private void processCooldowns() {
        int max = ecsWorld.getMaxEntityId();
        boolean[] alive = ecsWorld.getAlive();
        AttackCooldown[] cooldowns = ecsWorld.getComponentManager().getAttackCooldowns();

        for (int i = 0; i < max; i++) {
            if (!alive[i] || cooldowns[i] == null) {
                continue;
            }
            int remaining = cooldowns[i].decrement();
            if (remaining <= 0) {
                cooldowns[i] = null;
            }
        }
    }

    private void processAttacks() {
        int max = ecsWorld.getMaxEntityId();
        boolean[] alive = ecsWorld.getAlive();
        ComponentManager cm = ecsWorld.getComponentManager();
        AttackIntent[] intents = cm.getAttackIntents();
        AttackCooldown[] cooldowns = cm.getAttackCooldowns();
        Dead[] deads = cm.getDeads();
        Stats[] allStats = cm.getStats();
        Position[] positions = cm.getPositions();
        Dirty[] dirties = cm.getDirties();

        for (int attackerId = 0; attackerId < max; attackerId++) {
            if (!alive[attackerId] || intents[attackerId] == null) {
                continue;
            }

            int targetId = intents[attackerId].targetEntityId;
            intents[attackerId] = null; // consume the intent

            if (cooldowns[attackerId] != null) {
                continue;
            }
            if (!ecsWorld.entityExists(targetId) || deads[targetId] != null) {
                continue;
            }
            if (!isInRange(attackerId, targetId, positions)) {
                continue;
            }

            Stats attackerStats = allStats[attackerId];
            Stats targetStats = allStats[targetId];
            if (attackerStats == null || targetStats == null) {
                continue;
            }

            cooldowns[attackerId] = new AttackCooldown(attackCooldownTicks);

            int damage = CombatRules.calculateDamage(attackerStats, targetStats);
            CombatRules.applyDamage(targetStats, damage);
            if (dirties[targetId] == null) {
                dirties[targetId] = new Dirty(ecsWorld.getCurrentTick());
            } else {
                dirties[targetId].tick = ecsWorld.getCurrentTick();
            }

            if (CombatRules.isDead(targetStats)) {
                deads[targetId] = new Dead(attackerId);
            }
        }
    }

    private void processDeath() {
        int max = ecsWorld.getMaxEntityId();
        boolean[] alive = ecsWorld.getAlive();
        ComponentManager cm = ecsWorld.getComponentManager();
        Dead[] deads = cm.getDeads();
        Zone[] zones = cm.getZones();

        for (int i = 0; i < max; i++) {
            if (!alive[i] || deads[i] == null) {
                continue;
            }
            String zoneId = zones[i] != null ? zones[i].zoneId : "unknown";

            awardXpToKiller(i, cm);

            gameEventPort.onEntityDespawn(i, zoneId);
            spatialGridSystem.removeEntity(i);
            ecsWorld.destroyEntity(i);
        }
    }

    private void awardXpToKiller(int deadEntityId, ComponentManager cm) {
        Dead dead = cm.getDeads()[deadEntityId];
        if (dead == null || dead.killerId < 0) {
            return;
        }

        Monster monster = cm.getMonsters()[deadEntityId];
        if (monster == null) {
            return;
        }

        int killerId = dead.killerId;
        if (!ecsWorld.entityExists(killerId)) {
            return;
        }
        Experience killerExp = cm.getExperiences()[killerId];
        if (killerExp == null) {
            return;
        }

        LevelingRules.addXp(killerExp, monster.xpReward);
        Dirty[] dirties = cm.getDirties();
        if (dirties[killerId] == null) {
            dirties[killerId] = new Dirty(ecsWorld.getCurrentTick());
        } else {
            dirties[killerId].tick = ecsWorld.getCurrentTick();
        }

        LOG.log(System.Logger.Level.DEBUG,
                "Entity {0} gained {1} XP from killing {2}",
                killerId, monster.xpReward, deadEntityId);
    }

    private boolean isInRange(int attackerId, int targetId, Position[] positions) {
        Position attackerPos = positions[attackerId];
        Position targetPos = positions[targetId];
        if (attackerPos == null || targetPos == null) {
            return false;
        }
        return CombatRules.isInRange(attackerPos, targetPos, attackRange);
    }
}
