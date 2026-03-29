package com.grimoire.contracts.wire.dto;

import java.io.Serializable;

/**
 * Request to create a new group.
 */
public record CreateGroup(String groupName) implements Serializable {
}
