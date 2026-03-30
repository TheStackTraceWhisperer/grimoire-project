package com.grimoire.application.core.system;

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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Processes combat logic: cooldowns, attack resolution, death, and XP rewards.
 *
 * <p>
 * Each tick the system: (1) decrements {@link AttackCooldown} timers; (2)
 * resolves {@link AttackIntent} components using {@link CombatRules}; (3)
 * handles death — awarding XP, notifying via {@link GameEventPort}, and
 * destroying the dead entity.
 * </p>
 */
public class CombatSystem implements GameSystem {

    /** System logger. */
    private static final System.Logger LOG = System.getLogger(CombatSystem.class.getName());

    /** The ECS world. */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "EcsWorld is a managed collaborator, not external mutable data")
    private final EcsWorld ecsWorld;

    /** Port for entity despawn notifications. */
    private final GameEventPort gameEventPort;

    /** Spatial grid for entity cleanup on death. */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "SpatialGridSystem is a managed collaborator")
    private final SpatialGridSystem spatialGridSystem;

    /** Maximum attack range in world units. */
    private final double attackRange;

    /** Cooldown duration in ticks after each attack. */
    private final int attackCooldownTicks;

    /**
     * Creates a combat system.
     *
     * @param ecsWorld
     *            the ECS world
     * @param gameConfig
     *            configuration providing attack range and cooldown
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

    /**
     * Decrements all {@link AttackCooldown} timers, removing expired ones.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void processCooldowns() {
        List<String> entities = new ArrayList<>();
        for (String entityId : ecsWorld.getEntitiesWithComponent(AttackCooldown.class)) {
            entities.add(entityId);
        }

        for (String entityId : entities) {
            ecsWorld.getComponent(entityId, AttackCooldown.class).ifPresent(cooldown -> {
                int remaining = cooldown.ticksRemaining() - 1;
                if (remaining <= 0) {
                    ecsWorld.removeComponent(entityId, AttackCooldown.class);
                } else {
                    ecsWorld.addComponent(entityId, new AttackCooldown(remaining));
                }
            });
        }
    }

    /**
     * Resolves all pending {@link AttackIntent} components.
     */
    @SuppressWarnings({"PMD.CognitiveComplexity", "PMD.CyclomaticComplexity",
            "PMD.AvoidInstantiatingObjectsInLoops"})
    private void processAttacks() {
        List<String> attackers = new ArrayList<>();
        for (String entityId : ecsWorld.getEntitiesWithComponent(AttackIntent.class)) {
            attackers.add(entityId);
        }

        for (String attackerId : attackers) {
            Optional<AttackIntent> intentOpt = ecsWorld.getComponent(attackerId,
                    AttackIntent.class);
            if (intentOpt.isEmpty()) {
                continue;
            }

            String targetId = intentOpt.get().targetEntityId();
            ecsWorld.removeComponent(attackerId, AttackIntent.class);

            if (ecsWorld.hasComponent(attackerId, AttackCooldown.class)) {
                continue;
            }
            if (!ecsWorld.entityExists(targetId) || ecsWorld.hasComponent(targetId, Dead.class)) {
                continue;
            }
            if (!isInRange(attackerId, targetId)) {
                continue;
            }

            Optional<Stats> attackerStatsOpt = ecsWorld.getComponent(attackerId, Stats.class);
            Optional<Stats> targetStatsOpt = ecsWorld.getComponent(targetId, Stats.class);
            if (attackerStatsOpt.isEmpty() || targetStatsOpt.isEmpty()) {
                continue;
            }

            ecsWorld.addComponent(attackerId, new AttackCooldown(attackCooldownTicks));

            int damage = CombatRules.calculateDamage(attackerStatsOpt.get(), targetStatsOpt.get());
            Stats newTargetStats = CombatRules.applyDamage(targetStatsOpt.get(), damage);
            ecsWorld.addComponent(targetId, newTargetStats);
            ecsWorld.addComponent(targetId, new Dirty(ecsWorld.getCurrentTick()));

            if (CombatRules.isDead(newTargetStats)) {
                ecsWorld.addComponent(targetId, new Dead(attackerId));
            }
        }
    }

    /**
     * Handles entities marked as {@link Dead}: awards XP, fires notifications, and
     * destroys the entity.
     */
    private void processDeath() {
        List<String> deadEntities = new ArrayList<>();
        for (String entityId : ecsWorld.getEntitiesWithComponent(Dead.class)) {
            deadEntities.add(entityId);
        }

        for (String deadEntityId : deadEntities) {
            String zoneId = ecsWorld.getComponent(deadEntityId, Zone.class)
                    .map(Zone::zoneId)
                    .orElse("unknown");

            awardXpToKiller(deadEntityId);

            gameEventPort.onEntityDespawn(deadEntityId, zoneId);
            spatialGridSystem.removeEntity(deadEntityId);
            ecsWorld.destroyEntity(deadEntityId);
        }
    }

    /**
     * Awards XP to the killer if the dead entity was a {@link Monster}.
     *
     * @param deadEntityId
     *            the entity that died
     */
    private void awardXpToKiller(String deadEntityId) {
        Optional<Dead> deadOpt = ecsWorld.getComponent(deadEntityId, Dead.class);
        if (deadOpt.isEmpty() || deadOpt.get().killerId() == null) {
            return;
        }

        String killerId = deadOpt.get().killerId();
        Optional<Monster> monsterOpt = ecsWorld.getComponent(deadEntityId, Monster.class);
        if (monsterOpt.isEmpty()) {
            return;
        }

        if (!ecsWorld.entityExists(killerId)
                || !ecsWorld.hasComponent(killerId, Experience.class)) {
            return;
        }

        Experience currentExp = ecsWorld.getComponent(killerId, Experience.class).orElseThrow();
        Experience newExp = LevelingRules.addXp(currentExp, monsterOpt.get().xpReward());
        ecsWorld.addComponent(killerId, newExp);
        ecsWorld.addComponent(killerId, new Dirty(ecsWorld.getCurrentTick()));

        LOG.log(System.Logger.Level.DEBUG,
                "Entity {0} gained {1} XP from killing {2}",
                killerId, monsterOpt.get().xpReward(), deadEntityId);
    }

    /**
     * Checks whether the attacker is within attack range of the target.
     *
     * @param attackerId
     *            the attacker entity ID
     * @param targetId
     *            the target entity ID
     * @return {@code true} if the attacker is within range
     */
    private boolean isInRange(String attackerId, String targetId) {
        Optional<Position> attackerPos = ecsWorld.getComponent(attackerId, Position.class);
        Optional<Position> targetPos = ecsWorld.getComponent(targetId, Position.class);
        if (attackerPos.isEmpty() || targetPos.isEmpty()) {
            return false;
        }
        return CombatRules.isInRange(attackerPos.get(), targetPos.get(), attackRange);
    }
}
