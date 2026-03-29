package com.grimoire.contracts.wire.dto;

import java.io.Serializable;

/**
 * Entity despawn notification.
 */
public record EntityDespawn(String entityId) implements Serializable {
}
