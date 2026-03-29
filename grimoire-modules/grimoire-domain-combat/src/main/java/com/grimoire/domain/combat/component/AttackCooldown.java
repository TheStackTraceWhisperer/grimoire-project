package com.grimoire.domain.combat.component;

import com.grimoire.domain.core.component.Component;

/**
 * Component tracking attack cooldown for an entity.
 *
 * <p>
 * Prevents rapid-fire attacks by enforcing a cooldown period between attacks.
 * </p>
 *
 * @param ticksRemaining
 *            the number of ticks until the entity can attack again
 */
public record AttackCooldown(int ticksRemaining) implements Component {
}
