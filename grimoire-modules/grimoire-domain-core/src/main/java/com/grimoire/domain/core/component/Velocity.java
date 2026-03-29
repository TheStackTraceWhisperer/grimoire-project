package com.grimoire.domain.core.component;

/**
 * Velocity component for entity movement direction and speed.
 *
 * @param dx
 *            the velocity along the X axis
 * @param dy
 *            the velocity along the Y axis
 */
public record Velocity(double dx, double dy) implements Component {
}
