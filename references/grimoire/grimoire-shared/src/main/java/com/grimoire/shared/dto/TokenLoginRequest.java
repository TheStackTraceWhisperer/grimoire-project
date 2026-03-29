package com.grimoire.shared.dto;

import java.io.Serializable;

/**
 * Login request with OAuth2 access token from client to server.
 */
public record TokenLoginRequest(String accessToken) implements Serializable {
}
