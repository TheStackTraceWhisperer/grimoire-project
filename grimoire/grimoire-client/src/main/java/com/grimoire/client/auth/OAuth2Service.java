package com.grimoire.client.auth;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.awt.Desktop;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import com.sun.net.httpserver.HttpServer;

/**
 * OAuth2 authentication service for the client.
 * Handles the OAuth2 authorization code flow with PKCE.
 */
@Singleton
@Slf4j
public class OAuth2Service {
    
    private static final String CLIENT_ID = "grimoire-client";
    private static final String CLIENT_SECRET = "client-secret";
    private static final String KEYCLOAK_URL = "http://localhost:9000/realms/grimoire";
    private static final String REDIRECT_URI = "http://localhost:8889/authorized";
    private static final int CALLBACK_PORT = 8889;
    
    private final NetHttpTransport httpTransport = new NetHttpTransport();
    private final JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    
    private String authorizationCode;
    private HttpServer callbackServer;
    
    /**
     * Initiates the OAuth2 authorization code flow.
     * Opens browser for user to login and starts local HTTP server to receive callback.
     * 
     * @return the access token if authentication is successful, null otherwise
     */
    public String authenticate() {
        try {
            // Start local HTTP server to receive callback
            startCallbackServer();
            
            // Build authorization URL
            AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(
                    BearerToken.authorizationHeaderAccessMethod(),
                    httpTransport,
                    jsonFactory,
                    new GenericUrl(KEYCLOAK_URL + "/protocol/openid-connect/token"),
                    new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET),
                    CLIENT_ID,
                    KEYCLOAK_URL + "/protocol/openid-connect/auth")
                    .setScopes(Arrays.asList("openid", "profile"))
                    .build();
            
            String authorizationUrl = flow.newAuthorizationUrl()
                    .setRedirectUri(REDIRECT_URI)
                    .setState("state")
                    .build();
            
            log.info("Opening browser for authentication: {}", authorizationUrl);
            
            // Open browser for user to authenticate
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(authorizationUrl));
            } else {
                log.warn("Desktop not supported, please navigate to: {}", authorizationUrl);
            }
            
            // Wait for callback (timeout after 2 minutes)
            long startTime = System.currentTimeMillis();
            while (authorizationCode == null && System.currentTimeMillis() - startTime < 120000) {
                Thread.sleep(500);
            }
            
            if (authorizationCode == null) {
                log.error("Authentication timed out");
                stopCallbackServer();
                return null;
            }
            
            log.info("Authorization code received, exchanging for access token");
            
            // Exchange authorization code for access token
            TokenResponse tokenResponse = flow.newTokenRequest(authorizationCode)
                    .setRedirectUri(REDIRECT_URI)
                    .execute();
            
            stopCallbackServer();
            
            log.info("Access token obtained successfully");
            return tokenResponse.getAccessToken();
            
        } catch (Exception e) {
            log.error("Authentication failed", e);
            stopCallbackServer();
            return null;
        }
    }
    
    private void startCallbackServer() throws IOException {
        callbackServer = HttpServer.create(new InetSocketAddress(CALLBACK_PORT), 0);
        callbackServer.createContext("/authorized", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("code=")) {
                String code = query.split("code=")[1].split("&")[0];
                authorizationCode = code;
                
                // Send success response
                String response = "<html><body><h1>Authentication Successful!</h1><p>You can close this window and return to the game.</p></body></html>";
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
            } else {
                String response = "<html><body><h1>Authentication Failed</h1><p>Authorization code not received.</p></body></html>";
                exchange.sendResponseHeaders(400, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
            }
        });
        callbackServer.setExecutor(null);
        callbackServer.start();
        log.info("Callback server started on port {}", CALLBACK_PORT);
    }
    
    private void stopCallbackServer() {
        if (callbackServer != null) {
            callbackServer.stop(0);
            log.info("Callback server stopped");
        }
    }
}
