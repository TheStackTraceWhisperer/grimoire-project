package com.grimoire.domain.combat.rule;

import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.Stats;

/**
 * Pure domain rules for combat calculations.
 *
 * <p>
 * All methods are static and side-effect-free. No framework annotations, no
 * mutable state.
 * </p>
 */
public final class CombatRules {

    private CombatRules() {
        // Utility class
    }

    /**
     * Calculates damage dealt by an attacker to a target.
     *
     * <p>
     * Formula: {@code damage = attacker.attack - target.defense}, minimum 1.
     * </p>
     *
     * @param attacker
     *            the attacker's stats
     * @param target
     *            the target's stats
     * @return the damage amount (always ≥ 1)
     */
    public static int calculateDamage(Stats attacker, Stats target) {
        int rawDamage = attacker.attack() - target.defense();
        return Math.max(1, rawDamage);
    }

    /**
     * Returns a new {@link Stats} with HP reduced by the given damage, floored at
     * zero.
     *
     * @param target
     *            the current target stats
     * @param damage
     *            the damage amount to apply
     * @return a new Stats with updated HP
     */
    public static Stats applyDamage(Stats target, int damage) {
        int newHp = Math.max(0, target.hp() - damage);
        return new Stats(newHp, target.maxHp(), target.defense(), target.attack());
    }

    /**
     * Checks whether a target is dead (HP ≤ 0).
     *
     * @param stats
     *            the stats to check
     * @return {@code true} if the entity should be considered dead
     */
    public static boolean isDead(Stats stats) {
        return stats.hp() <= 0;
    }

    /**
     * Checks if two positions are within the specified attack range.
     *
     * @param attacker
     *            the attacker's position
     * @param target
     *            the target's position
     * @param attackRange
     *            the maximum attack distance
     * @return {@code true} if the squared distance is ≤ attackRange²
     */
    public static boolean isInRange(Position attacker, Position target, double attackRange) {
        double dx = attacker.x() - target.x();
        double dy = attacker.y() - target.y();
        return (dx * dx + dy * dy) <= (attackRange * attackRange);
    }
}
