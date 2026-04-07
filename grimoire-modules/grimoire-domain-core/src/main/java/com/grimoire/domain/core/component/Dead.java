package com.grimoire.domain.core.component;

/**
 * Dead marker component for entities that have been killed.
 *
 * <p>
 * Entities with this component should be despawned on the next sync and then
 * removed from the ECS world.
 * </p>
 */
public class Dead implements Component {

    /**
     * The entity ID of the killer, or -1 for environmental deaths.
     */
    public int killerId;

    /**
     * No-arg constructor for array pre-allocation.
     */
    public Dead() {
        this.killerId = -1;
        // default values
    }

    /**
     * Creates a dead marker with the given killer.
     *
     * @param killerId
     *            the entity ID of the killer, or -1 for environmental deaths
     */
    public Dead(int killerId) {
        this.killerId = killerId;
    }

    /**
     * Zero-allocation update.
     *
     * @param newKillerId
     *            the new killer entity ID
     */
    public void update(int newKillerId) {
        this.killerId = newKillerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Dead d)) {
            return false;
        }
        return killerId == d.killerId;
    }

    @Override
    public int hashCode() {
        return killerId;
    }

    @Override
    public String toString() {
        return "Dead[killerId=" + killerId + "]";
    }
}
