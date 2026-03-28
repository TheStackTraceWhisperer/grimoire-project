package com.grimoire.server.component;
import com.grimoire.ecs.Component;

/**
 * Velocity component for entity movement.
 */
public record Velocity(double dx, double dy) implements Component {
}
