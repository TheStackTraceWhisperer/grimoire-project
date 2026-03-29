package com.grimoire.contracts.wire.dto;

import java.io.Serializable;

/**
 * Failed login response from server to client.
 */
public record LoginFailure(String reason) implements Serializable {
}
