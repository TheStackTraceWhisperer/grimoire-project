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
import com.grimoire.domain.core.component.*;

import java.util.Objects;

import static com.grimoire.application.core.ecs.ComponentManager.*;

/**
 * Processes combat logic: cooldowns, attack resolution, death, and XP rewards.
 *
 * <p>
 * Iterates the dense active-entity array using bitwise signature checks.
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
    public void tick(long currentTick) {
        processCooldowns();
        processAttacks();
        processDeath();
    }

    private void processCooldowns() {
        int[] active = ecsWorld.getActiveEntities();
        int count = ecsWorld.getActiveCount();
        ComponentManager cm = ecsWorld.getComponentManager();
        long[] sigs = cm.getSignatures();
        AttackCooldown[] cooldowns = cm.getAttackCooldowns();

        for (int j = 0; j < count; j++) {
            int i = active[j];
            if ((sigs[i] & BIT_ATTACK_COOLDOWN) != BIT_ATTACK_COOLDOWN) {
                continue;
            }
            int remaining = cooldowns[i].decrement();
            if (remaining <= 0) {
                cm.removeAttackCooldown(i);
            }
        }
    }

    private void processAttacks() {
        int[] active = ecsWorld.getActiveEntities();
        int count = ecsWorld.getActiveCount();
        ComponentManager cm = ecsWorld.getComponentManager();
        long[] sigs = cm.getSignatures();
        AttackIntent[] intents = cm.getAttackIntents();
        Stats[] allStats = cm.getStats();
        Position[] positions = cm.getPositions();

        for (int j = 0; j < count; j++) {
            int attackerId = active[j];
            if ((sigs[attackerId] & BIT_ATTACK_INTENT) != BIT_ATTACK_INTENT) {
                continue;
            }

            int targetId = intents[attackerId].targetEntityId;
            cm.removeAttackIntent(attackerId); // consume the intent

            if ((sigs[attackerId] & BIT_ATTACK_COOLDOWN) != 0) {
                continue;
            }
            if (!ecsWorld.entityExists(targetId) || (sigs[targetId] & BIT_DEAD) != 0) {
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

            cm.addAttackCooldown(attackerId, attackCooldownTicks);

            int damage = CombatRules.calculateDamage(attackerStats, targetStats);
            CombatRules.applyDamage(targetStats, damage);
            cm.addDirty(targetId, ecsWorld.getCurrentTick());

            if (CombatRules.isDead(targetStats)) {
                cm.addDead(targetId, attackerId);
            }
        }
    }

    private void processDeath() {
        int[] active = ecsWorld.getActiveEntities();
        int count = ecsWorld.getActiveCount();
        ComponentManager cm = ecsWorld.getComponentManager();
        long[] sigs = cm.getSignatures();
        Zone[] zones = cm.getZones();

        for (int j = count - 1; j >= 0; j--) {
            int i = active[j];
            if ((sigs[i] & BIT_DEAD) != BIT_DEAD) {
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
        cm.addDirty(killerId, ecsWorld.getCurrentTick());

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
