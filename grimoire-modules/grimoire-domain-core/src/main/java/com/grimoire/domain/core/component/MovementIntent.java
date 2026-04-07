package com.grimoire.domain.core.component;

/**
 * Movement intent component for player movement input.
 */
public class MovementIntent implements Component {

    /**
     * The target X coordinate the entity wants to move towards.
     */
    public double targetX;

    /**
     * The target Y coordinate the entity wants to move towards.
     */
    public double targetY;

    /**
     * No-arg constructor for array pre-allocation.
     */
    public MovementIntent() {
        // default values
    }

    /**
     * Creates a movement intent.
     *
     * @param targetX
     *            target X coordinate
     * @param targetY
     *            target Y coordinate
     */
    public MovementIntent(double targetX, double targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
    }

    /**
     * Zero-allocation update.
     *
     * @param newTargetX
     *            new target X
     * @param newTargetY
     *            new target Y
     */
    public void update(double newTargetX, double newTargetY) {
        this.targetX = newTargetX;
        this.targetY = newTargetY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MovementIntent m)) {
            return false;
        }
        return Double.compare(m.targetX, targetX) == 0 && Double.compare(m.targetY, targetY) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(targetX) * 31 + Double.hashCode(targetY);
    }

    @Override
    public String toString() {
        return "MovementIntent[targetX=" + targetX + ", targetY=" + targetY + "]";
    }
}
