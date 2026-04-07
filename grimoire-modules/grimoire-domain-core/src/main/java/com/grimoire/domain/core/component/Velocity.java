package com.grimoire.domain.core.component;

/**
 * Velocity component for entity movement direction and speed.
 */
public class Velocity implements Component {

    /**
     * The velocity along the X axis.
     */
    public double dx;

    /**
     * The velocity along the Y axis.
     */
    public double dy;

    /**
     * No-arg constructor for array pre-allocation.
     */
    public Velocity() {
        // default values
    }

    /**
     * Creates a velocity with the given deltas.
     *
     * @param dx
     *            velocity along X
     * @param dy
     *            velocity along Y
     */
    public Velocity(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Zero-allocation update of velocity.
     *
     * @param newDx
     *            new X velocity
     * @param newDy
     *            new Y velocity
     */
    public void update(double newDx, double newDy) {
        this.dx = newDx;
        this.dy = newDy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Velocity v)) {
            return false;
        }
        return Double.compare(v.dx, dx) == 0 && Double.compare(v.dy, dy) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(dx) * 31 + Double.hashCode(dy);
    }

    @Override
    public String toString() {
        return "Velocity[dx=" + dx + ", dy=" + dy + "]";
    }
}
