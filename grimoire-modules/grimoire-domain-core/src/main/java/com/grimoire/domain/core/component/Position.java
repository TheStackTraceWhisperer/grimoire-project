package com.grimoire.domain.core.component;

/**
 * Position component for entity location in world space.
 */
public class Position implements Component {

    /**
     * The X coordinate in world units.
     */
    public double x;

    /**
     * The Y coordinate in world units.
     */
    public double y;

    /**
     * No-arg constructor for array pre-allocation.
     */
    public Position() {
        // default values
    }

    /**
     * Creates a position at the given coordinates.
     *
     * @param x
     *            the X coordinate
     * @param y
     *            the Y coordinate
     */
    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Zero-allocation update of both coordinates.
     *
     * @param newX
     *            the new X coordinate
     * @param newY
     *            the new Y coordinate
     */
    public void update(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }

    /**
     * Zero-allocation translation by delta values.
     *
     * @param dx
     *            delta X
     * @param dy
     *            delta Y
     */
    public void translate(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Position p)) {
            return false;
        }
        return Double.compare(p.x, x) == 0 && Double.compare(p.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(x) * 31 + Double.hashCode(y);
    }

    @Override
    public String toString() {
        return "Position[x=" + x + ", y=" + y + "]";
    }
}
