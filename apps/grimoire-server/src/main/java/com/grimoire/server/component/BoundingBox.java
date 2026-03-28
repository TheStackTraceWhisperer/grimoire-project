package com.grimoire.server.component;
import com.grimoire.ecs.Component;

/**
 * Bounding box component for collision detection.
 */
public record BoundingBox(double width, double height) implements Component {
}
