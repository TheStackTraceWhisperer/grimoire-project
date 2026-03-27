package com.grimoire.client.ui;

import com.grimoire.client.JavaFxApplication;
import com.grimoire.client.network.NetworkClient;
import com.grimoire.shared.dto.CharacterListResponse;
import com.grimoire.shared.dto.CharacterSelectionRequest;
import com.grimoire.shared.protocol.GamePacket;
import com.grimoire.shared.protocol.PacketType;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for character selection screen.
 */
@Singleton
@Slf4j
public class CharacterSelectionController {
    
    @FXML
    private ListView<String> characterList;
    
    @FXML
    private Button selectButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Label infoLabel;
    
    private final NetworkClient networkClient;
    private final JavaFxApplication application;
    private CharacterListResponse response;
    
    public CharacterSelectionController(NetworkClient networkClient, JavaFxApplication application) {
        this.networkClient = networkClient;
        this.application = application;
    }
    
    @FXML
    private void initialize() {
        characterList.getItems().clear();
        response = application.getCharacterListResponse();
        if (response != null) {
            for (CharacterListResponse.CharacterInfo character : response.characters()) {
                characterList.getItems().add(
                    String.format("%s (Level %d) - %s", 
                        character.name(), character.level(), character.lastZone())
                );
            }
            infoLabel.setText(String.format("Select a character (%d available)", response.characters().size()));
        }
    }
    
    @FXML
    private void handleSelect() {
        int selectedIndex = characterList.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && response != null) {
            CharacterListResponse.CharacterInfo selectedCharacter = response.characters().get(selectedIndex);
            log.info("Selected character: {}", selectedCharacter.name());
            
            CharacterSelectionRequest request = new CharacterSelectionRequest(selectedCharacter.id());
            GamePacket packet = new GamePacket(PacketType.C2S_CHARACTER_SELECTION, request);
            networkClient.send(packet);
        } else {
            log.warn("No character selected");
        }
    }
    
    @FXML
    private void handleRefresh() {
        networkClient.send(new GamePacket(PacketType.C2S_REQUEST_CHARACTER_LIST, null));
    }
}
