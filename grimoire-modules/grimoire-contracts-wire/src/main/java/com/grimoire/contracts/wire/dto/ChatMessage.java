package com.grimoire.contracts.wire.dto;

import java.io.Serializable;

/**
 * Chat message from client to server.
 */
public record ChatMessage(String message) implements Serializable {
}
