package com.grimoire.client.ui;

import com.grimoire.client.auth.OAuth2Service;
import com.grimoire.client.network.NetworkClient;
import com.grimoire.shared.dto.TokenLoginRequest;
import com.grimoire.shared.protocol.GamePacket;
import com.grimoire.shared.protocol.PacketType;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for the login screen.
 */
@Singleton
@Slf4j
public class LoginController {
    
    @FXML
    private TextField serverField;
    
    @FXML
    private Button oauthLoginButton;
    
    private final NetworkClient networkClient;
    private final OAuth2Service oauth2Service;
    
    @Inject
    public LoginController(NetworkClient networkClient, OAuth2Service oauth2Service) {
        this.networkClient = networkClient;
        this.oauth2Service = oauth2Service;
    }
    
    @FXML
    private void initialize() {
        serverField.setText("localhost:8888");
    }
    
    @FXML
    private void handleOAuthLogin() {
        String server = serverField.getText();
        
        // Disable button during authentication
        oauthLoginButton.setDisable(true);
        
        // Run OAuth2 flow in background thread
        new Thread(() -> {
            try {
                log.info("Starting OAuth2 authentication flow");
                String accessToken = oauth2Service.authenticate();
                
                if (accessToken != null) {
                    // Connect to game server
                    String[] parts = server.split(":");
                    String host = parts[0];
                    int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 8888;
                    
                    log.info("Connecting to game server at {}:{}", host, port);
                    networkClient.connect(host, port);
                    
                    // Send token login request
                    TokenLoginRequest request = new TokenLoginRequest(accessToken);
                    GamePacket packet = new GamePacket(PacketType.C2S_TOKEN_LOGIN_REQUEST, request);
                    networkClient.send(packet);
                    
                    log.info("Token login request sent");
                } else {
                    log.error("OAuth2 authentication failed - no access token received");
                    Platform.runLater(() -> oauthLoginButton.setDisable(false));
                }
            } catch (Exception e) {
                log.error("OAuth2 login failed", e);
                Platform.runLater(() -> oauthLoginButton.setDisable(false));
            }
        }).start();
    }
}
