package com.grimoire.server.component;
import com.grimoire.ecs.Component;

/**
 * NPC AI component with behavior type.
 */
public record NpcAi(AiType type) implements Component {
    
    public enum AiType {
        FRIENDLY_WANDER,
        HOSTILE_AGGRO_MELEE
    }
}
