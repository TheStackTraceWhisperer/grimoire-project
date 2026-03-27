package com.grimoire.client.util;

import com.grimoire.client.JavaFxApplication;
import com.grimoire.client.auth.OAuth2Service;
import com.grimoire.client.network.NetworkClient;
import com.grimoire.client.state.ClientEcsWorld;
import com.grimoire.shared.component.PositionDTO;
import com.grimoire.shared.component.RenderableDTO;
import com.grimoire.shared.dto.CharacterListResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;

/**
 * Generates screenshots for documentation.
 */
@MicronautTest
@ExtendWith(ApplicationExtension.class)
class GenerateScreenshots {

    @Inject
    ApplicationContext context;

    @Inject
    ClientEcsWorld clientEcsWorld;

    private Stage primaryStage;

    @Start
    private void start(Stage stage) {
        this.primaryStage = stage;
    }

    @MockBean(OAuth2Service.class)
    OAuth2Service mockOAuth2Service() {
        return Mockito.mock(OAuth2Service.class);
    }

    @MockBean(NetworkClient.class)
    NetworkClient mockNetworkClient() {
        return Mockito.mock(NetworkClient.class);
    }

    @Test
    void generateLoginScreenshot() throws Exception {
        JavaFxApplication app = context.getBean(JavaFxApplication.class);
        String outputPath = "docs/screenshots/login.png";
        ScreenshotCapture.captureLoginScreen(app, primaryStage, outputPath);
    }

    @Test
    void generateCharacterSelectionScreenshot() throws Exception {
        JavaFxApplication app = context.getBean(JavaFxApplication.class);
        String outputPath = "docs/screenshots/character_selection.png";
        List<CharacterListResponse.CharacterInfo> characters = List.of(
            new CharacterListResponse.CharacterInfo(1L, "Archmage Elara", 45, "Mystic Forest"),
            new CharacterListResponse.CharacterInfo(2L, "Sir Galahad", 38, "Castle Grounds"),
            new CharacterListResponse.CharacterInfo(3L, "Shadow Rogue", 42, "Dark Alley"),
            new CharacterListResponse.CharacterInfo(4L, "Druid Willow", 40, "Ancient Grove")
        );
        CharacterListResponse response = new CharacterListResponse("test-session", characters);
        ScreenshotCapture.captureCharacterSelectionScreen(app, primaryStage, response, outputPath);
    }

    @Test
    void generateGameScreenshot() throws Exception {
        JavaFxApplication app = context.getBean(JavaFxApplication.class);
        String outputPath = "docs/screenshots/game.png";
        
        // Spawn some entities first
        clientEcsWorld.spawnEntity(new com.grimoire.shared.dto.EntitySpawn(
            "player-1",
            List.of(new PositionDTO(200, 150), new RenderableDTO("visual-player", "Hero"))
        ));
        
        clientEcsWorld.spawnEntity(new com.grimoire.shared.dto.EntitySpawn(
            "npc-1",
            List.of(new PositionDTO(350, 200), new RenderableDTO("visual-npc-friendly", "Merchant"))
        ));
        
        clientEcsWorld.spawnEntity(new com.grimoire.shared.dto.EntitySpawn(
            "monster-1",
            List.of(new PositionDTO(500, 300), new RenderableDTO("visual-monster-rat", "Giant Rat"))
        ));
        
        clientEcsWorld.spawnEntity(new com.grimoire.shared.dto.EntitySpawn(
            "monster-2",
            List.of(new PositionDTO(300, 400), new RenderableDTO("visual-monster-wolf", "Dire Wolf"))
        ));
        
        clientEcsWorld.spawnEntity(new com.grimoire.shared.dto.EntitySpawn(
            "portal-1",
            List.of(new PositionDTO(600, 450), new RenderableDTO("visual-portal", "Portal"))
        ));
        
        ScreenshotCapture.captureGameScreen(app, primaryStage, outputPath);
    }
}
