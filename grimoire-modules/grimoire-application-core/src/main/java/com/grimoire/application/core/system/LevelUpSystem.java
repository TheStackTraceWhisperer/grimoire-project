package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.GameSystem;
import com.grimoire.domain.core.component.Dirty;
import com.grimoire.domain.core.component.Experience;
import com.grimoire.domain.core.component.Stats;
import com.grimoire.domain.combat.rule.LevelingRules;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Processes level-up progression for entities with {@link Experience}.
 *
 * <p>
 * When an entity's current XP reaches the threshold, this system applies
 * level-ups via {@link LevelingRules} — boosting stats, rolling over excess XP,
 * and scaling the next threshold. Multiple level-ups per tick are supported.
 * </p>
 */
public class LevelUpSystem implements GameSystem {

    /** The ECS world. */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "EcsWorld is a managed collaborator, not external mutable data")
    private final EcsWorld ecsWorld;

    /**
     * Creates a level-up system.
     *
     * @param ecsWorld
     *            the ECS world
     */
    public LevelUpSystem(EcsWorld ecsWorld) {
        this.ecsWorld = Objects.requireNonNull(ecsWorld, "ecsWorld must not be null");
    }

    @Override
    public void tick(float deltaTime) {
        List<String> entities = new ArrayList<>();
        for (String entityId : ecsWorld.getEntitiesWithComponent(Experience.class)) {
            entities.add(entityId);
        }

        for (String entityId : entities) {
            processLevelUps(entityId);
        }
    }

    /**
     * Applies all pending level-ups for a single entity.
     *
     * @param entityId
     *            the entity to check
     */
    private void processLevelUps(String entityId) {
        Optional<Experience> expOpt = ecsWorld.getComponent(entityId, Experience.class);
        if (expOpt.isEmpty()) {
            return;
        }

        Experience exp = expOpt.get();
        if (!LevelingRules.canLevelUp(exp)) {
            return;
        }

        Optional<Stats> statsOpt = ecsWorld.getComponent(entityId, Stats.class);
        Stats stats = statsOpt.orElse(null);

        while (LevelingRules.canLevelUp(exp)) {
            exp = LevelingRules.applyLevelUp(exp);
            if (stats != null) {
                stats = LevelingRules.boostStatsForLevelUp(stats);
            }
        }

        ecsWorld.addComponent(entityId, exp);
        if (stats != null) {
            ecsWorld.addComponent(entityId, stats);
        }
        ecsWorld.addComponent(entityId, new Dirty(ecsWorld.getCurrentTick()));
    }
}
