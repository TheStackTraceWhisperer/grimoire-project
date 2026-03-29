package com.grimoire.domain.combat.component;

import com.grimoire.domain.core.component.Component;

/**
 * NPC AI component defining behaviour type.
 *
 * @param type
 *            the AI behaviour type
 */
public record NpcAi(AiType type) implements Component {

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
