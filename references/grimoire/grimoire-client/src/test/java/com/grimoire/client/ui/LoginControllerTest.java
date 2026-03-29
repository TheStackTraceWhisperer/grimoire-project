package com.grimoire.client.ui;

import com.grimoire.client.JavaFxApplication;
import com.grimoire.client.auth.OAuth2Service;
import com.grimoire.client.network.NetworkClient;
import io.micronaut.context.ApplicationContext;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * UI test for the LoginController.
 * This test runs in a headless environment (e.g., CI)
 * by inheriting the surefire-plugin args from pom.xml.
 */
@MicronautTest
@ExtendWith(ApplicationExtension.class)
class LoginControllerTest {

    @Inject
    ApplicationContext context;

    @Inject
    OAuth2Service mockOAuth2Service; // This is the mock bean

    @Inject
    NetworkClient mockNetworkClient; // This is the mock bean

    private Stage primaryStage;

    /**
     * Set up the JavaFX application with the Micronaut context.
     */
    @Start
    private void start(Stage stage) {
        // Manually launch the Micronaut-aware JavaFX app
        JavaFxApplication app = context.getBean(JavaFxApplication.class);
        Platform.runLater(() -> {
            try {
                // We store the stage to close it later
                this.primaryStage = stage;
                app.start(stage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @AfterEach
    void tearDown() {
        Platform.runLater(() -> {
            if (primaryStage != null) {
                primaryStage.close();
            }
        });
    }

    /**
     * We must mock the services that the LoginController depends on
     * to prevent real network/browser calls.
     */
    @MockBean(OAuth2Service.class)
    OAuth2Service mockOAuth2Service() {
        return Mockito.mock(OAuth2Service.class);
    }

    @MockBean(NetworkClient.class)
    NetworkClient mockNetworkClient() {
        return Mockito.mock(NetworkClient.class);
    }

    @Test
    void testOAuthLoginButton_onClick(FxRobot robot) throws Exception {
        // Arrange
        String fakeAccessToken = "fake-test-token-123";
        // Mock the OAuth2 service to return a fake token immediately
        when(mockOAuth2Service.authenticate()).thenReturn(fakeAccessToken);

        // Act
        // Find the button by its fx:id
        Button oauthButton = robot.lookup("#oauthLoginButton").queryButton();
        assertNotNull(oauthButton, "OAuth login button not found");
        
        // Simulate a click
        robot.clickOn(oauthButton);

        // Assert
        // Verify that the NetworkClient.connect() and .send() methods
        // were called as a result of the click.
        // We use Mockito.verify with a timeout to wait for the background thread.
        Mockito.verify(mockNetworkClient, Mockito.timeout(1000))
                .connect("localhost", 8888);
        
        Mockito.verify(mockNetworkClient, Mockito.timeout(1000))
                .send(Mockito.any());
    }
}
