package io.grimoire.launcher.ui;

import io.micronaut.context.ApplicationContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * JavaFX application for the launcher.
 */
@Slf4j
public class JavaFxLauncherApplication extends Application {
    private static ApplicationContext applicationContext;
    
    /**
     * Sets the Micronaut application context.
     */
    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        log.info("Starting Launcher UI");
        
        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/io/grimoire/launcher/ui/launcher.fxml"));
        
        // Set controller factory to use Micronaut
        loader.setControllerFactory(applicationContext::getBean);
        
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 600, 300);
        primaryStage.setTitle("Grimoire Launcher");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
