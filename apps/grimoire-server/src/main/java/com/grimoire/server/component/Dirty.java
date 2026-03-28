package com.grimoire.server.component;
import com.grimoire.ecs.Component;

/**
 * Dirty component marking entities that have changed and need network sync.
 */
public record Dirty(long tick) implements Component {
}
