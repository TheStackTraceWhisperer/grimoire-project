package com.grimoire.contracts.wire.dto;

import java.io.Serializable;

/**
 * Response for leaving a group.
 */
public record LeaveGroupResponse(boolean success, String message) implements Serializable {
}
