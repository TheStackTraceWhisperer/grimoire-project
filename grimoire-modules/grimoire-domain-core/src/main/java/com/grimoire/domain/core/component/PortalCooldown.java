package com.grimoire.domain.core.component;

/**
 * Portal cooldown component preventing immediate re-entry after a zone
 * transition.
 *
 * @param ticksRemaining
 *            the number of game ticks until the cooldown expires
 */
public record PortalCooldown(long ticksRemaining) implements Component {
}
