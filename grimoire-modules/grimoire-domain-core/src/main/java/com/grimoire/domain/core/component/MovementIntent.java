package com.grimoire.domain.core.component;

/**
 * Movement intent component for player movement input.
 *
 * @param targetX
 *            the target X coordinate the entity wants to move towards
 * @param targetY
 *            the target Y coordinate the entity wants to move towards
 */
public record MovementIntent(double targetX, double targetY) implements Component {
}
