package com.grimoire.domain.combat.component;

import com.grimoire.domain.core.component.Component;

/**
 * Attack intent component for initiating combat.
 *
 * <p>
 * Added when an entity wants to attack another entity. The combat system
 * processes this intent and applies damage if valid.
 * </p>
 */
public class AttackIntent implements Component {

    /** The ID of the entity to attack. */
    public int targetEntityId;

    /** No-arg constructor for array pre-allocation. */
    public AttackIntent() {
        this.targetEntityId = -1;
    }

    /**
     * Creates an attack intent.
     *
     * @param targetEntityId
     *            the target entity ID
     */
    public AttackIntent(int targetEntityId) {
        this.targetEntityId = targetEntityId;
    }

    /**
     * Zero-allocation update.
     *
     * @param newTargetEntityId
     *            the new target entity ID
     */
    public void update(int newTargetEntityId) {
        this.targetEntityId = newTargetEntityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AttackIntent a)) {
            return false;
        }
        return targetEntityId == a.targetEntityId;
    }

    @Override
    public int hashCode() {
        return targetEntityId;
    }

    @Override
    public String toString() {
        return "AttackIntent[targetEntityId=" + targetEntityId + "]";
    }
}
