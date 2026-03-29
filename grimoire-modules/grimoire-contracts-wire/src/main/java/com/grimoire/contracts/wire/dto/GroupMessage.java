package com.grimoire.contracts.wire.dto;

import java.io.Serializable;

/**
 * Group message from client to server.
 */
public record GroupMessage(String groupName, String message) implements Serializable {
}
