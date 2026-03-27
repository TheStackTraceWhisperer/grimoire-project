package com.grimoire.shared.dto;

import java.io.Serializable;

/**
 * Chat broadcast from server to client.
 */
public record ChatBroadcast(String sender, String message) implements Serializable {
}
