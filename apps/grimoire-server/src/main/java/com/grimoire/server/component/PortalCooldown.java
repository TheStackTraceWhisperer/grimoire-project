package com.grimoire.server.component;
import com.grimoire.ecs.Component;

/**
 * Portal cooldown component preventing immediate re-entry.
 */
public record PortalCooldown(long ticksRemaining) implements Component {
}
