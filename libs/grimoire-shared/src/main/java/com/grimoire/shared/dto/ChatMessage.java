package com.grimoire.shared.dto;

import java.io.Serializable;

/**
 * Chat message from client to server.
 */
public record ChatMessage(String message) implements Serializable {
}
