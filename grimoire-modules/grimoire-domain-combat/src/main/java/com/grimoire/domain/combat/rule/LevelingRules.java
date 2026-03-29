package com.grimoire.domain.combat.rule;

import com.grimoire.domain.core.component.Experience;
import com.grimoire.domain.core.component.Stats;

/**
 * Pure domain rules for level-up progression.
 *
 * <p>
 * All methods are static and side-effect-free. Leveling follows a simple loop:
 * while {@code currentXp ≥ xpToNextLevel}, a level-up is triggered.
 * </p>
 *
 * <p>
 * On each level-up:
 * </p>
 * <ul>
 * <li>Excess XP carries over</li>
 * <li>The XP threshold scales by {@value #XP_SCALING_FACTOR}×</li>
 * <li>Stats are boosted ({@value #HP_PER_LEVEL} maxHp,
 * {@value #ATTACK_PER_LEVEL} attack, {@value #DEFENSE_PER_LEVEL} defense)</li>
 * </ul>
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
        return experience.currentXp() >= experience.xpToNextLevel();
    }

    /**
     * Calculates the experience component after a single level-up.
     *
     * <p>
     * Excess XP rolls over and the threshold scales by
     * {@value #XP_SCALING_FACTOR}×.
     * </p>
     *
     * @param experience
     *            the current experience (must satisfy {@link #canLevelUp})
     * @return a new Experience with rolled-over XP and increased threshold
     * @throws IllegalArgumentException
     *             if the entity does not have enough XP to level up
     */
    public static Experience applyLevelUp(Experience experience) {
        if (!canLevelUp(experience)) {
            throw new IllegalArgumentException("Not enough XP to level up");
        }
        int remainingXp = experience.currentXp() - experience.xpToNextLevel();
        int newThreshold = Math.max(
                experience.xpToNextLevel() + 1,
                (int) (experience.xpToNextLevel() * XP_SCALING_FACTOR));
        return new Experience(remainingXp, newThreshold);
    }

    /**
     * Boosts stats for a single level-up.
     *
     * <p>
     * Increases maxHp by {@value #HP_PER_LEVEL}, attack by
     * {@value #ATTACK_PER_LEVEL}, defense by {@value #DEFENSE_PER_LEVEL}. Current
     * HP is healed by the maxHp increase, capped at the new maximum.
     * </p>
     *
     * @param stats
     *            the current stats
     * @return a new Stats with boosted attributes
     */
    public static Stats boostStatsForLevelUp(Stats stats) {
        int newMaxHp = stats.maxHp() + HP_PER_LEVEL;
        int newHp = Math.min(stats.hp() + HP_PER_LEVEL, newMaxHp);
        int newAttack = stats.attack() + ATTACK_PER_LEVEL;
        int newDefense = stats.defense() + DEFENSE_PER_LEVEL;
        return new Stats(newHp, newMaxHp, newDefense, newAttack);
    }

    /**
     * Applies all pending level-ups in a loop, returning the final experience.
     *
     * <p>
     * Use this when an entity may have accumulated enough XP for multiple level-ups
     * at once (e.g., large XP reward).
     * </p>
     *
     * @param experience
     *            the current experience
     * @return the experience after all possible level-ups
     */
    public static Experience applyAllLevelUps(Experience experience) {
        Experience result = experience;
        while (canLevelUp(result)) {
            result = applyLevelUp(result);
        }
        return result;
    }

    /**
     * Adds XP to an experience component, returning the updated experience.
     *
     * @param experience
     *            the current experience
     * @param xpGain
     *            the XP to add (must be non-negative)
     * @return a new Experience with the added XP
     * @throws IllegalArgumentException
     *             if xpGain is negative
     */
    public static Experience addXp(Experience experience, int xpGain) {
        if (xpGain < 0) {
            throw new IllegalArgumentException("XP gain must be non-negative: " + xpGain);
        }
        return new Experience(experience.currentXp() + xpGain, experience.xpToNextLevel());
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
        Experience temp = experience;
        while (canLevelUp(temp)) {
            temp = applyLevelUp(temp);
            count++;
        }
        return count;
    }
}
