package com.grimoire.server.component;

/**
 * NPC AI component with behavior type.
 */
public record NpcAi(AiType type) implements Component {
    
    public enum AiType {
        FRIENDLY_WANDER,
        HOSTILE_AGGRO_MELEE
    }
}
