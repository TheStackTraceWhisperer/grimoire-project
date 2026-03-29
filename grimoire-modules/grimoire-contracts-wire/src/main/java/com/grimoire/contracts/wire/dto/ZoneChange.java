package com.grimoire.contracts.wire.dto;

import java.io.Serializable;

/**
 * Zone change notification from server to client.
 */
public record ZoneChange(String newZoneId, double spawnX, double spawnY) implements Serializable {
}
