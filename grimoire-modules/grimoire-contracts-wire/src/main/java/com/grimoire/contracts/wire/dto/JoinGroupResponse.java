package com.grimoire.contracts.wire.dto;

import java.io.Serializable;

/**
 * Response for joining a group.
 */
public record JoinGroupResponse(boolean success, String message) implements Serializable {
}
