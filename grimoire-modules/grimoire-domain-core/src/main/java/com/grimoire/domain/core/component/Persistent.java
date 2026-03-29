package com.grimoire.domain.core.component;

/**
 * Persistent component associating an entity with a player account.
 *
 * @param accountId
 *            the external account identifier (e.g. Keycloak subject)
 */
public record Persistent(String accountId) implements Component {
}
