package com.grimoire.ecs;

/**
 * Test component for monster.
 */
public record Monster(MonsterType type) implements Component {
    
    /**
     * Types of monsters for testing.
     */
    public enum MonsterType {
        RAT,
        GOBLIN,
        DRAGON
    }
}

