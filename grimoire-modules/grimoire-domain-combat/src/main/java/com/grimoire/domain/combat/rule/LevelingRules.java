package com.grimoire.domain.combat.rule;

import com.grimoire.domain.core.component.Experience;
import com.grimoire.domain.core.component.Stats;

/**
 * Pure domain rules for level-up progression.
 *
 * <p>
 * Leveling follows a simple loop: while {@code currentXp ≥ xpToNextLevel}, a
 * level-up is triggered. Methods now mutate components in place for
 * zero-allocation operation.
 * </p>
 */
public final class LevelingRules {

    /** HP increase per level-up. */
    public static final int HP_PER_LEVEL = 10;

    /** Attack increase per level-up. */
    public static final int ATTACK_PER_LEVEL = 2;

    /** Defense increase per level-up. */
    public static final int DEFENSE_PER_LEVEL = 1;

    /** Multiplier applied to xpToNextLevel on each level-up. */
    public static final double XP_SCALING_FACTOR = 1.5;

    private LevelingRules() {
    }

    /**
     * Returns {@code true} if the entity has enough XP to level up.
     *
     * @param experience
     *            the entity's current experience
     * @return whether a level-up is available
     */
    public static boolean canLevelUp(Experience experience) {
        return experience.currentXp >= experience.xpToNextLevel;
    }

    /**
     * Applies a single level-up in place. Excess XP rolls over, threshold scales.
     *
     * @param experience
     *            the experience (mutated in place)
     * @throws IllegalArgumentException
     *             if not enough XP
     */
    public static void applyLevelUp(Experience experience) {
        if (!canLevelUp(experience)) {
            throw new IllegalArgumentException("Not enough XP to level up");
        }
        int remainingXp = experience.currentXp - experience.xpToNextLevel;
        int newThreshold = Math.max(
                experience.xpToNextLevel + 1,
                (int) (experience.xpToNextLevel * XP_SCALING_FACTOR));
        experience.currentXp = remainingXp;
        experience.xpToNextLevel = newThreshold;
    }

    /**
     * Boosts stats for a single level-up in place.
     *
     * @param stats
     *            the current stats (mutated in place)
     */
    public static void boostStatsForLevelUp(Stats stats) {
        stats.maxHp += HP_PER_LEVEL;
        stats.hp = Math.min(stats.hp + HP_PER_LEVEL, stats.maxHp);
        stats.attack += ATTACK_PER_LEVEL;
        stats.defense += DEFENSE_PER_LEVEL;
    }

    /**
     * Applies all pending level-ups in a loop, mutating in place.
     *
     * @param experience
     *            the current experience (mutated)
     */
    public static void applyAllLevelUps(Experience experience) {
        while (canLevelUp(experience)) {
            applyLevelUp(experience);
        }
    }

    /**
     * Adds XP to an experience component in place.
     *
     * @param experience
     *            the current experience (mutated)
     * @param xpGain
     *            the XP to add (must be non-negative)
     * @throws IllegalArgumentException
     *             if xpGain is negative
     */
    public static void addXp(Experience experience, int xpGain) {
        if (xpGain < 0) {
            throw new IllegalArgumentException("XP gain must be non-negative: " + xpGain);
        }
        experience.currentXp += xpGain;
    }

    /**
     * Counts how many level-ups would occur for the given experience.
     *
     * @param experience
     *            the experience to check
     * @return the number of pending level-ups (0 or more)
     */
    public static int countPendingLevelUps(Experience experience) {
        int count = 0;
        int xp = experience.currentXp;
        int threshold = experience.xpToNextLevel;
        while (xp >= threshold) {
            xp -= threshold;
            threshold = Math.max(threshold + 1, (int) (threshold * XP_SCALING_FACTOR));
            count++;
        }
        return count;
    }
}
