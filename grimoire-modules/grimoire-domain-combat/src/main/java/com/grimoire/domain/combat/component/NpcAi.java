package com.grimoire.domain.combat.component;

import com.grimoire.domain.core.component.Component;

/**
 * NPC AI component defining behaviour type.
 */
public class NpcAi implements Component {

    /** The AI behaviour type. */
    public AiType type;

    /** No-arg constructor for array pre-allocation. */
    public NpcAi() {
        // default values
    }

    /**
     * Creates an NPC AI component.
     *
     * @param type
     *            the AI behaviour type
     */
    public NpcAi(AiType type) {
        this.type = type;
    }

    /**
     * Zero-allocation update.
     *
     * @param newType
     *            the new AI type
     */
    public void update(AiType newType) {
        this.type = newType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NpcAi n)) {
            return false;
        }
        return type == n.type;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "NpcAi[type=" + type + "]";
    }

    /**
     * AI behaviour classification.
     */
    public enum AiType {
        /** Wanders peacefully, does not attack. */
        FRIENDLY_WANDER,
        /** Attacks players within aggro range using melee. */
        HOSTILE_AGGRO_MELEE
    }
}
