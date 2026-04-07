package com.grimoire.domain.core.component;

/**
 * Dirty marker component for entities that have changed and need network
 * synchronisation.
 */
public class Dirty implements Component {

    /**
     * The game tick at which the change occurred.
     */
    public long tick;

    /**
     * No-arg constructor for array pre-allocation.
     */
    public Dirty() {
        // default values
    }

    /**
     * Creates a dirty marker at the given tick.
     *
     * @param tick
     *            the game tick at which the change occurred
     */
    public Dirty(long tick) {
        this.tick = tick;
    }

    /**
     * Zero-allocation update.
     *
     * @param newTick
     *            the new tick value
     */
    public void update(long newTick) {
        this.tick = newTick;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Dirty d)) {
            return false;
        }
        return tick == d.tick;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(tick);
    }

    @Override
    public String toString() {
        return "Dirty[tick=" + tick + "]";
    }
}
