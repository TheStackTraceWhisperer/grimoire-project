package com.grimoire.ecs;

/**
 * Test component for stats.
 */
public record Stats(int health, int mana, int attack, int defense) implements Component {
}

