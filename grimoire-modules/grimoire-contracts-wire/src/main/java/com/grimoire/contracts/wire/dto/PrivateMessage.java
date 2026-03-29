package com.grimoire.contracts.wire.dto;

import java.io.Serializable;

/**
 * Private message from client to server.
 */
public record PrivateMessage(String recipientName, String message) implements Serializable {
}
