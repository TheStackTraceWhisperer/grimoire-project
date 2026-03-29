package com.grimoire.contracts.wire.dto;

import java.io.Serializable;

/**
 * Chat broadcast from server to client.
 */
public record ChatBroadcast(String sender, String message) implements Serializable {
}
