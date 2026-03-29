package com.grimoire.contracts.wire.dto;

import java.io.Serializable;

/**
 * Movement intent from client to server.
 */
public record MovementIntent(double targetX, double targetY) implements Serializable {
}
