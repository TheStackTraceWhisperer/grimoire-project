package com.grimoire.client;

import com.grimoire.client.preferences.WindowPreferences;
import com.grimoire.shared.dto.CharacterListResponse;
import io.micronaut.context.ApplicationContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX application bridge for Micronaut dependency injection.
 */
@Singleton
public class JavaFxApplication extends Application {
    
    private static ApplicationContext context;
    private Stage primaryStage;
    private CharacterListResponse characterListResponse;
    private WindowPreferences windowPreferences;
    
    @Inject
    public JavaFxApplication(ApplicationContext context) {
        JavaFxApplication.context = context;
    }
    
    public void launchApp(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }
    
    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        this.windowPreferences = context.getBean(WindowPreferences.class);
        
        primaryStage.setTitle("Grimoire MMO");
        
        showLoginScene();
        
        // Restore the maximized state from user preferences
        if (windowPreferences.isMaximized()) {
            primaryStage.setMaximized(true);
        }
        
        // Add listener to save maximize state changes
        primaryStage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
            windowPreferences.saveMaximizedState(newValue);
        });
        
        primaryStage.show();
    }
    
    public void showLoginScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        loader.setControllerFactory(context::getBean);
        Scene scene = new Scene(loader.load(), 400, 300);
        primaryStage.setScene(scene);
    }
    
    public void showCharacterSelectionScene(CharacterListResponse response) throws IOException {
        this.characterListResponse = response;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/character_selection.fxml"));
        loader.setControllerFactory(context::getBean);
        Scene scene = new Scene(loader.load(), 600, 400);
        primaryStage.setScene(scene);
    }
    
    public CharacterListResponse getCharacterListResponse() {
        return characterListResponse;
    }
    
    public void showGameScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/game.fxml"));
        loader.setControllerFactory(context::getBean);
        Scene scene = new Scene(loader.load(), 800, 600);
        primaryStage.setScene(scene);
    }
    
    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
    }
}
