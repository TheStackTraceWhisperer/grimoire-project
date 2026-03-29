package com.grimoire.domain.core.component;

/**
 * Portal component for zone transitions.
 *
 * @param targetZoneId
 *            the zone the portal leads to
 * @param targetPortalId
 *            the exit portal identifier in the target zone
 */
public record Portal(String targetZoneId, String targetPortalId) implements Component {
}
