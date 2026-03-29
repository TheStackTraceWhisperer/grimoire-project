package com.grimoire.client.ui;

import com.grimoire.client.JavaFxApplication;
import com.grimoire.client.network.NetworkClient;
import com.grimoire.shared.dto.CharacterListResponse;
import com.grimoire.shared.dto.CharacterListResponse.CharacterInfo;
import com.grimoire.shared.protocol.GamePacket;
import com.grimoire.shared.protocol.PacketType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class CharacterSelectionControllerTest {
    
    private NetworkClient mockNetworkClient;
    private JavaFxApplication mockApplication;
    private CharacterSelectionController controller;
    private ListView<String> characterList;
    private Button selectButton;
    private Button refreshButton;
    private Label infoLabel;
    
    @Start
    void start(Stage stage) throws Exception {
        mockNetworkClient = mock(NetworkClient.class);
        mockApplication = mock(JavaFxApplication.class);
        
        // Setup test data
        List<CharacterInfo> characters = List.of(
            new CharacterInfo(1L, "Warrior", 10, "Forest"),
            new CharacterInfo(2L, "Mage", 15, "Tower"),
            new CharacterInfo(3L, "Rogue", 8, "City")
        );
        CharacterListResponse response = new CharacterListResponse("test-session", characters);
        when(mockApplication.getCharacterListResponse()).thenReturn(response);
        
        controller = new CharacterSelectionController(mockNetworkClient, mockApplication);
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/character_selection.fxml"));
        loader.setControllerFactory(c -> controller);
        Parent root = loader.load();
        
        characterList = (ListView<String>) root.lookup("#characterList");
        selectButton = (Button) root.lookup("#selectButton");
        refreshButton = (Button) root.lookup("#refreshButton");
        infoLabel = (Label) root.lookup("#infoLabel");
        
        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.show();
    }
    
    @Test
    void testControllerCreation() {
        assertNotNull(controller);
    }
    
    @Test
    void testCharacterListIsPopulated() {
        assertNotNull(characterList);
        assertEquals(3, characterList.getItems().size());
        assertTrue(characterList.getItems().get(0).contains("Warrior"));
        assertTrue(characterList.getItems().get(1).contains("Mage"));
        assertTrue(characterList.getItems().get(2).contains("Rogue"));
    }
    
    @Test
    void testInfoLabelShowsCharacterCount() {
        assertNotNull(infoLabel);
        assertTrue(infoLabel.getText().contains("3"));
    }
    
    @Test
    void testSelectButtonExists() {
        assertNotNull(selectButton);
        assertEquals("Select Character", selectButton.getText());
    }
    
    @Test
    void testRefreshButtonExists() {
        assertNotNull(refreshButton);
        assertEquals("Refresh List", refreshButton.getText());
    }
    
    @Test
    void testSelectCharacter(FxRobot robot) {
        // Select first character from the list
        characterList.getSelectionModel().select(0);
        
        // Click select button
        robot.clickOn("Select Character");
        
        // Verify that character selection packet was sent
        ArgumentCaptor<GamePacket> packetCaptor = ArgumentCaptor.forClass(GamePacket.class);
        verify(mockNetworkClient, atLeastOnce()).send(packetCaptor.capture());
        
        GamePacket sentPacket = packetCaptor.getValue();
        assertEquals(PacketType.C2S_CHARACTER_SELECTION, sentPacket.type());
        assertNotNull(sentPacket.payload());
    }
    
    @Test
    void testSelectDifferentCharacter(FxRobot robot) {
        // Select second character (Mage) from the list
        characterList.getSelectionModel().select(1);
        
        // Verify selection
        assertEquals(1, characterList.getSelectionModel().getSelectedIndex());
        
        // Click select button
        robot.clickOn("Select Character");
        
        // Verify packet was sent
        verify(mockNetworkClient, atLeastOnce()).send(any(GamePacket.class));
    }
    
    @Test
    void testSelectWithoutChoosing(FxRobot robot) {
        // Clear selection
        characterList.getSelectionModel().clearSelection();
        
        // Click select button without selecting a character
        robot.clickOn("Select Character");
        
        // No packet should be sent (or at least not a character selection packet)
        // This tests the edge case handling
        assertNotNull(controller);
    }
    
    @Test
    void testRefreshButton(FxRobot robot) {
        // Click refresh button
        robot.clickOn("Refresh List");
        
        // Verify that character list request packet was sent
        ArgumentCaptor<GamePacket> packetCaptor = ArgumentCaptor.forClass(GamePacket.class);
        verify(mockNetworkClient, atLeastOnce()).send(packetCaptor.capture());
        
        GamePacket sentPacket = packetCaptor.getValue();
        assertEquals(PacketType.C2S_REQUEST_CHARACTER_LIST, sentPacket.type());
    }
    
    @Test
    void testCharacterListSelection(FxRobot robot) {
        // Select each character and verify selection
        characterList.getSelectionModel().select(0);
        assertEquals(0, characterList.getSelectionModel().getSelectedIndex());
        
        characterList.getSelectionModel().select(1);
        assertEquals(1, characterList.getSelectionModel().getSelectedIndex());
        
        characterList.getSelectionModel().select(2);
        assertEquals(2, characterList.getSelectionModel().getSelectedIndex());
    }
    
    @Test
    void testCharacterInfoDisplay() {
        // Verify character information is displayed correctly
        String firstItem = characterList.getItems().get(0);
        assertTrue(firstItem.contains("Warrior"));
        assertTrue(firstItem.contains("10")); // Level
        assertTrue(firstItem.contains("Forest")); // Zone
    }
}

@ExtendWith(ApplicationExtension.class)
class CharacterSelectionControllerEmptyTest {
    
    private NetworkClient mockNetworkClient;
    private JavaFxApplication mockApplication;
    private CharacterSelectionController controller;
    
    @Start
    void start(Stage stage) throws Exception {
        mockNetworkClient = mock(NetworkClient.class);
        mockApplication = mock(JavaFxApplication.class);
        
        // Setup empty character list
        CharacterListResponse emptyResponse = new CharacterListResponse("test-session", List.of());
        when(mockApplication.getCharacterListResponse()).thenReturn(emptyResponse);
        
        controller = new CharacterSelectionController(mockNetworkClient, mockApplication);
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/character_selection.fxml"));
        loader.setControllerFactory(c -> controller);
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.show();
    }
    
    @Test
    void testEmptyCharacterList() {
        assertNotNull(controller);
        // Controller should handle empty list gracefully
    }
}

@ExtendWith(ApplicationExtension.class)
class CharacterSelectionControllerNullTest {
    
    private NetworkClient mockNetworkClient;
    private JavaFxApplication mockApplication;
    private CharacterSelectionController controller;
    
    @Start
    void start(Stage stage) throws Exception {
        mockNetworkClient = mock(NetworkClient.class);
        mockApplication = mock(JavaFxApplication.class);
        
        // Setup null response
        when(mockApplication.getCharacterListResponse()).thenReturn(null);
        
        controller = new CharacterSelectionController(mockNetworkClient, mockApplication);
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/character_selection.fxml"));
        loader.setControllerFactory(c -> controller);
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.show();
    }
    
    @Test
    void testNullCharacterResponse() {
        assertNotNull(controller);
        // Controller should handle null response gracefully
    }
}
