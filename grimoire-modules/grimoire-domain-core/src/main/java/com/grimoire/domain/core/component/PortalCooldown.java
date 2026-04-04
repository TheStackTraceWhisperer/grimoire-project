package com.grimoire.domain.core.component;

/**
 * Portal cooldown component preventing immediate re-entry after a zone
 * transition.
 */
public class PortalCooldown implements Component {

    /** The number of game ticks until the cooldown expires. */
    public long ticksRemaining;

    /** No-arg constructor for array pre-allocation. */
    public PortalCooldown() {
        // default values
    }

    /**
     * Creates a portal cooldown.
     *
     * @param ticksRemaining
     *            ticks until expiry
     */
    public PortalCooldown(long ticksRemaining) {
        this.ticksRemaining = ticksRemaining;
    }

    /**
     * Zero-allocation update.
     *
     * @param newTicksRemaining
     *            new remaining ticks
     */
    public void update(long newTicksRemaining) {
        this.ticksRemaining = newTicksRemaining;
    }

    /**
     * Decrements the cooldown by one tick.
     *
     * @return the new remaining ticks
     */
    public long decrement() {
        this.ticksRemaining--;
        return this.ticksRemaining;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PortalCooldown p)) {
            return false;
        }
        return ticksRemaining == p.ticksRemaining;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(ticksRemaining);
    }

    @Override
    public String toString() {
        return "PortalCooldown[ticksRemaining=" + ticksRemaining + "]";
    }
}
