package com.grimoire.shared.dto;

import java.io.Serializable;

/**
 * Response for group creation.
 */
public record CreateGroupResponse(boolean success, String message, Long groupId) implements Serializable {
}
