package com.grimoire.domain.core.component;

/**
 * Dirty marker component for entities that have changed and need network
 * synchronisation.
 *
 * @param tick
 *            the game tick at which the change occurred
 */
public record Dirty(long tick) implements Component {
}
