package com.grimoire.server.component;

/**
 * Attack intent component for initiating combat.
 * 
 * <p>This component is added when an entity wants to attack another entity.
 * The CombatSystem processes this intent and applies damage if valid.</p>
 */
public record AttackIntent(String targetEntityId) implements Component {
}
