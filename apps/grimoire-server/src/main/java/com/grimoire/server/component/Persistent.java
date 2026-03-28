package com.grimoire.server.component;
import com.grimoire.ecs.Component;

/**
 * Persistent component for player account association.
 */
public record Persistent(String accountId) implements Component {
}
