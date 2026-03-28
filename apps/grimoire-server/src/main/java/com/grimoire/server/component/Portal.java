package com.grimoire.server.component;
import com.grimoire.ecs.Component;

/**
 * Portal component for zone transitions.
 */
public record Portal(String targetZoneId, String targetPortalId) implements Component {
}
