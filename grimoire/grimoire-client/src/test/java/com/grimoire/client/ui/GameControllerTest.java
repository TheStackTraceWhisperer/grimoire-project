package com.grimoire.client.ui;

import com.grimoire.client.network.NetworkClient;
import com.grimoire.client.state.ClientEcsWorld;
import com.grimoire.shared.component.ComponentDTO;
import com.grimoire.shared.component.PositionDTO;
import com.grimoire.shared.component.RenderableDTO;
import com.grimoire.shared.protocol.GamePacket;
import com.grimoire.shared.protocol.PacketType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class GameControllerTest {
    
    private ClientEcsWorld mockEcsWorld;
    private NetworkClient mockNetworkClient;
    private GameController gameController;
    private Canvas gameCanvas;
    
    @Start
    void start(Stage stage) throws Exception {
        mockEcsWorld = mock(ClientEcsWorld.class);
        mockNetworkClient = mock(NetworkClient.class);
        
        // Setup mock entities for rendering
        ObservableMap<String, Map<Class<? extends ComponentDTO>, ComponentDTO>> entities = 
            FXCollections.observableHashMap();
        Map<Class<? extends ComponentDTO>, ComponentDTO> playerComponents = new HashMap<>();
        playerComponents.put(PositionDTO.class, new PositionDTO(100.0, 100.0));
        playerComponents.put(RenderableDTO.class, new RenderableDTO("visual-player", "TestPlayer"));
        entities.put("player-1", playerComponents);
        
        when(mockEcsWorld.getEntities()).thenReturn(entities);
        
        gameController = new GameController(mockEcsWorld, mockNetworkClient);
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/game.fxml"));
        loader.setControllerFactory(c -> gameController);
        Parent root = loader.load();
        
        gameCanvas = (Canvas) root.lookup("#gameCanvas");
        
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    @Test
    void testGameControllerCreation() {
        assertNotNull(gameController);
    }
    
    @Test
    void testCanvasIsInitialized() {
        assertNotNull(gameCanvas);
        assertEquals(800, gameCanvas.getWidth());
        assertEquals(600, gameCanvas.getHeight());
    }
    
    @Test
    void testCanvasClickSendsMovementIntent(FxRobot robot) {
        // Click on the canvas
        robot.clickOn(gameCanvas, MouseButton.PRIMARY);
        
        // Verify that a movement intent was sent
        ArgumentCaptor<GamePacket> packetCaptor = ArgumentCaptor.forClass(GamePacket.class);
        verify(mockNetworkClient, atLeastOnce()).send(packetCaptor.capture());
        
        GamePacket sentPacket = packetCaptor.getValue();
        assertEquals(PacketType.C2S_MOVEMENT_INTENT, sentPacket.type());
        assertNotNull(sentPacket.payload());
    }
    
    @Test
    void testCanvasClickAtSpecificPosition(FxRobot robot) {
        // Click at a specific position
        robot.clickOn(gameCanvas, MouseButton.PRIMARY);
        
        // Wait for the click to be processed
        robot.sleep(100);
        
        // Verify movement intent was sent
        verify(mockNetworkClient, atLeastOnce()).send(any(GamePacket.class));
    }
    
    @Test
    void testRenderLoopStops() {
        // The render loop should start on initialization
        assertNotNull(gameController);
        
        // Stop should not throw exception
        gameController.stop();
        
        // Controller should still be valid
        assertNotNull(gameController);
    }
    
    @Test
    void testMultipleClicksSendMultipleIntents(FxRobot robot) {
        // Click multiple times
        robot.clickOn(gameCanvas, MouseButton.PRIMARY);
        robot.sleep(50);
        robot.clickOn(gameCanvas, MouseButton.PRIMARY);
        robot.sleep(50);
        robot.clickOn(gameCanvas, MouseButton.PRIMARY);
        
        // Verify multiple packets were sent
        verify(mockNetworkClient, atLeast(3)).send(any(GamePacket.class));
    }
    
    @Test
    void testRenderingWithMultipleEntities(FxRobot robot) {
        // Add more entities
        ObservableMap<String, Map<Class<? extends ComponentDTO>, ComponentDTO>> entities = 
            FXCollections.observableHashMap();
        
        // Add player
        Map<Class<? extends ComponentDTO>, ComponentDTO> playerComponents = new HashMap<>();
        playerComponents.put(PositionDTO.class, new PositionDTO(100.0, 100.0));
        playerComponents.put(RenderableDTO.class, new RenderableDTO("visual-player", "Player"));
        entities.put("player-1", playerComponents);
        
        // Add monster
        Map<Class<? extends ComponentDTO>, ComponentDTO> monsterComponents = new HashMap<>();
        monsterComponents.put(PositionDTO.class, new PositionDTO(200.0, 200.0));
        monsterComponents.put(RenderableDTO.class, new RenderableDTO("visual-monster-rat", "Rat"));
        entities.put("monster-1", monsterComponents);
        
        // Add NPC
        Map<Class<? extends ComponentDTO>, ComponentDTO> npcComponents = new HashMap<>();
        npcComponents.put(PositionDTO.class, new PositionDTO(300.0, 300.0));
        npcComponents.put(RenderableDTO.class, new RenderableDTO("visual-npc-friendly", "Shopkeeper"));
        entities.put("npc-1", npcComponents);
        
        when(mockEcsWorld.getEntities()).thenReturn(entities);
        
        // Give time for render loop to process
        robot.sleep(200);
        
        // Canvas should still be valid and rendering
        assertNotNull(gameCanvas);
        assertNotNull(gameCanvas.getGraphicsContext2D());
    }
}
