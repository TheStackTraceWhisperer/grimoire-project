package com.grimoire.domain.core.component;

/**
 * Position component for entity location in world space.
 *
 * @param x
 *            the X coordinate in world units
 * @param y
 *            the Y coordinate in world units
 */
public record Position(double x, double y) implements Component {
}
