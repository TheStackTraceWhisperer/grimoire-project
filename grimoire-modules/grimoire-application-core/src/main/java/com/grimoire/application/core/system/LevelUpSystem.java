package com.grimoire.application.core.system;

import com.grimoire.application.core.ecs.ComponentManager;
import com.grimoire.application.core.ecs.EcsWorld;
import com.grimoire.application.core.ecs.GameSystem;
import com.grimoire.domain.combat.rule.LevelingRules;
import com.grimoire.domain.core.component.Experience;
import com.grimoire.domain.core.component.Stats;

import java.util.Objects;

import static com.grimoire.application.core.ecs.ComponentManager.BIT_EXPERIENCE;

/**
 * Processes level-up progression for entities with {@link Experience}.
 *
 * <p>
 * Iterates the dense active-entity array using a bitwise signature check. When
 * an entity's current XP reaches the threshold, level-ups are applied in place
 * via {@link LevelingRules}.
 * </p>
 */
public class LevelUpSystem implements GameSystem {

    private static final long REQUIRED_MASK = BIT_EXPERIENCE;

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
    public void tick(long currentTick) {
        int[] active = ecsWorld.getActiveEntities();
        int count = ecsWorld.getActiveCount();
        ComponentManager cm = ecsWorld.getComponentManager();
        long[] sigs = cm.getSignatures();
        Experience[] experiences = cm.getExperiences();
        Stats[] allStats = cm.getStats();

        for (int j = 0; j < count; j++) {
            int i = active[j];
            if ((sigs[i] & REQUIRED_MASK) != REQUIRED_MASK) {
                continue;
            }
            processLevelUps(i, experiences[i], allStats[i], cm);
        }
    }

    private void processLevelUps(int entityId, Experience exp, Stats stats,
            ComponentManager cm) {
        if (!LevelingRules.canLevelUp(exp)) {
            return;
        }

        while (LevelingRules.canLevelUp(exp)) {
            LevelingRules.applyLevelUp(exp);
            if (stats != null) {
                LevelingRules.boostStatsForLevelUp(stats);
            }
        }

        cm.addDirty(entityId, ecsWorld.getCurrentTick());
    }
}
