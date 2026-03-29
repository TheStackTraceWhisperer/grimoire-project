package com.grimoire.contracts.wire.dto;

import java.io.Serializable;

/**
 * Request to leave a group.
 */
public record LeaveGroup(String groupName) implements Serializable {
}
