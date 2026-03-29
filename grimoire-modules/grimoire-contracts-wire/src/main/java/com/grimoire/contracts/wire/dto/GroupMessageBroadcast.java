package com.grimoire.contracts.wire.dto;

import java.io.Serializable;

/**
 * Group message broadcast from server to clients in the group.
 */
public record GroupMessageBroadcast(String groupName, String sender, String message) implements Serializable {
}
