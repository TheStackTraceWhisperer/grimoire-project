package com.grimoire.domain.combat.component;

import com.grimoire.domain.core.component.Component;

/**
 * Attack intent component for initiating combat.
 *
 * <p>
 * Added when an entity wants to attack another entity. The combat system
 * processes this intent and applies damage if valid.
 * </p>
 *
 * @param targetEntityId
 *            the ID of the entity to attack
 */
public record AttackIntent(String targetEntityId) implements Component {
}
