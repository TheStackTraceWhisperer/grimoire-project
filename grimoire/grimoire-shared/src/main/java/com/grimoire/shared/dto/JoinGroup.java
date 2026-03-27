package com.grimoire.shared.dto;

import java.io.Serializable;

/**
 * Request to join a group.
 */
public record JoinGroup(String groupName) implements Serializable {
}
