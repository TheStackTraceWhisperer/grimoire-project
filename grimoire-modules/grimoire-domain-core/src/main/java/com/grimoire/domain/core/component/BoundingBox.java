package com.grimoire.domain.core.component;

/**
 * Bounding box component for collision detection.
 *
 * @param width
 *            the width of the bounding box in world units
 * @param height
 *            the height of the bounding box in world units
 */
public record BoundingBox(double width, double height) implements Component {
}
