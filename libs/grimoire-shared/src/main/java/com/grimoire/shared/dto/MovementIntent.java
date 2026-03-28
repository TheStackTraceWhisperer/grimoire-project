package com.grimoire.shared.dto;

import java.io.Serializable;

/**
 * Movement intent from client to server.
 */
public record MovementIntent(double targetX, double targetY) implements Serializable {
}
