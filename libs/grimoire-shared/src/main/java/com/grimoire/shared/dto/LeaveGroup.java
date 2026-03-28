package com.grimoire.shared.dto;

import java.io.Serializable;

/**
 * Request to leave a group.
 */
public record LeaveGroup(String groupName) implements Serializable {
}
