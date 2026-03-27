package com.grimoire.server.component;

/**
 * Movement intent component for player movement input.
 */
public record MovementIntent(double targetX, double targetY) implements Component {
}
