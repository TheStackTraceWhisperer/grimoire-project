package com.grimoire.server.component;
import com.grimoire.ecs.Component;

/**
 * Position component for entity location.
 */
public record Position(double x, double y) implements Component {
}
