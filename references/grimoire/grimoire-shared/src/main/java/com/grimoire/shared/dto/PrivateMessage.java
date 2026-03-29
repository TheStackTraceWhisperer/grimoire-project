package com.grimoire.shared.dto;

import java.io.Serializable;

/**
 * Private message from client to server.
 */
public record PrivateMessage(String recipientName, String message) implements Serializable {
}
