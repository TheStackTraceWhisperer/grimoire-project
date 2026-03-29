package com.grimoire.domain.core.component;

/**
 * Stats component for entity health and combat attributes.
 *
 * @param hp
 *            current hit points
 * @param maxHp
 *            maximum hit points
 * @param defense
 *            defense rating (reduces incoming damage)
 * @param attack
 *            attack rating (determines outgoing damage)
 */
public record Stats(int hp, int maxHp, int defense, int attack) implements Component {
}
