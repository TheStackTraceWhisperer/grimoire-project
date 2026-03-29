package io.grimoire.launcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grimoire.launcher.ui.JavaFxLauncherApplication;
import io.micronaut.context.ApplicationContext;
import javafx.application.Application;

/**
 * Main entry point for the Grimoire Launcher.
 */
public class LauncherMain {
    
    public static void main(String[] args) {
        // Start Micronaut context
        ApplicationContext context = ApplicationContext.run();
        
        // Ensure ObjectMapper is available
        context.getBean(ObjectMapper.class);
        
        // Set context for JavaFX app
        JavaFxLauncherApplication.setApplicationContext(context);
        
        // Launch JavaFX application
        Application.launch(JavaFxLauncherApplication.class, args);
    }
}
