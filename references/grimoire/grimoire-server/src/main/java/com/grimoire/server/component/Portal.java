package com.grimoire.server.component;

/**
 * Portal component for zone transitions.
 */
public record Portal(String targetZoneId, String targetPortalId) implements Component {
}
