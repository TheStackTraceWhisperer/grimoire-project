package com.grimoire.server.component;
import com.grimoire.ecs.Component;

/**
 * Stats component for entity health and combat attributes.
 */
public record Stats(int hp, int maxHp, int defense, int attack) implements Component {
}
