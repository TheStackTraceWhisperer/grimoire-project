package com.grimoire.domain.combat.component;

import com.grimoire.domain.core.component.Component;

/**
 * Component tracking attack cooldown for an entity.
 */
public class AttackCooldown implements Component {

    /** The number of ticks until the entity can attack again. */
    public int ticksRemaining;

    /** No-arg constructor for array pre-allocation. */
    public AttackCooldown() {
        // default values
    }

    /**
     * Creates an attack cooldown.
     *
     * @param ticksRemaining
     *            ticks until next attack
     */
    public AttackCooldown(int ticksRemaining) {
        this.ticksRemaining = ticksRemaining;
    }

    /**
     * Zero-allocation update.
     *
     * @param newTicksRemaining
     *            new remaining ticks
     */
    public void update(int newTicksRemaining) {
        this.ticksRemaining = newTicksRemaining;
    }

    /**
     * Decrements the cooldown by one tick.
     *
     * @return the new remaining ticks
     */
    public int decrement() {
        this.ticksRemaining--;
        return this.ticksRemaining;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AttackCooldown a)) {
            return false;
        }
        return ticksRemaining == a.ticksRemaining;
    }

    @Override
    public int hashCode() {
        return ticksRemaining;
    }

    @Override
    public String toString() {
        return "AttackCooldown[ticksRemaining=" + ticksRemaining + "]";
    }
}
