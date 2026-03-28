package com.grimoire.shared.dto;

import java.io.Serializable;

/**
 * Private message broadcast from server to client.
 */
public record PrivateMessageBroadcast(String sender, String message) implements Serializable {
}
