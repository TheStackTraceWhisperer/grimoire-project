package com.grimoire.server.component;

/**
 * Portal cooldown component preventing immediate re-entry.
 */
public record PortalCooldown(long ticksRemaining) implements Component {
}
