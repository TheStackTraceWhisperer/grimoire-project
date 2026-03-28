package com.grimoire.server.component;
import com.grimoire.ecs.Component;

/**
 * Zone component indicating which zone an entity is in.
 */
public record Zone(String zoneId) implements Component {
}
