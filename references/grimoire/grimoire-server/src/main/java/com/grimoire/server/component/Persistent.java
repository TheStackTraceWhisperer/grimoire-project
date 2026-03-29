package com.grimoire.server.component;

/**
 * Persistent component for player account association.
 */
public record Persistent(String accountId) implements Component {
}
