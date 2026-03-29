package com.grimoire.domain.core.component;

/**
 * Zone component indicating which zone an entity belongs to.
 *
 * @param zoneId
 *            the identifier of the zone
 */
public record Zone(String zoneId) implements Component {
}
