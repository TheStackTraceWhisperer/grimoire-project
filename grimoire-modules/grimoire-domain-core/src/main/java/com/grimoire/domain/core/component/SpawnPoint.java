package com.grimoire.domain.core.component;

/**
 * Spawn point component storing an NPC's origin and leash radius.
 */
public class SpawnPoint implements Component {

    /** The X coordinate of the spawn point. */
    public double x;

    /** The Y coordinate of the spawn point. */
    public double y;

    /** The maximum distance from the spawn point before the NPC resets. */
    public double leashRadius;

    /** No-arg constructor for array pre-allocation. */
    public SpawnPoint() {
        // default values
    }

    /**
     * Creates a spawn point.
     *
     * @param x
     *            the X coordinate
     * @param y
     *            the Y coordinate
     * @param leashRadius
     *            the leash radius
     */
    public SpawnPoint(double x, double y, double leashRadius) {
        this.x = x;
        this.y = y;
        this.leashRadius = leashRadius;
    }

    /**
     * Zero-allocation update.
     *
     * @param newX
     *            new X coordinate
     * @param newY
     *            new Y coordinate
     * @param newLeashRadius
     *            new leash radius
     */
    public void update(double newX, double newY, double newLeashRadius) {
        this.x = newX;
        this.y = newY;
        this.leashRadius = newLeashRadius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpawnPoint s)) {
            return false;
        }
        return Double.compare(s.x, x) == 0
                && Double.compare(s.y, y) == 0
                && Double.compare(s.leashRadius, leashRadius) == 0;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        result = 31 * result + Double.hashCode(leashRadius);
        return result;
    }

    @Override
    public String toString() {
        return "SpawnPoint[x=" + x + ", y=" + y + ", leashRadius=" + leashRadius + "]";
    }
}
