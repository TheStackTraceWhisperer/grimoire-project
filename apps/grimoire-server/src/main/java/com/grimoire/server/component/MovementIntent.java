package com.grimoire.server.component;
import com.grimoire.ecs.Component;

/**
 * Movement intent component for player movement input.
 */
public record MovementIntent(double targetX, double targetY) implements Component {
}
