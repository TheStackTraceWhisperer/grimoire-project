package com.grimoire.shared.dto;

import java.io.Serializable;

/**
 * Response for joining a group.
 */
public record JoinGroupResponse(boolean success, String message) implements Serializable {
}
