package io.grimoire.launcher.service;

import io.grimoire.launcher.model.LocalCache;
import io.grimoire.launcher.model.Manifest;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for launching the game client using ProcessBuilder.
 */
@Singleton
@Slf4j
public class GameLauncher {
    private static final String FX_MODULE_PATH_PLACEHOLDER = "%FX_MODULE_PATH%";
    
    /**
     * Launches the game client with the given local cache configuration.
     */
    public void launchGame(LocalCache cache) throws IOException {
        log.info("Launching game version: {}", cache.getManifest().getVersion());
        
        Manifest manifest = cache.getManifest();
        List<String> command = new ArrayList<>();
        
        // Java executable
        String javaHome = System.getProperty("java.home");
        String javaExe = javaHome + File.separator + "bin" + File.separator + "java";
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            javaExe += ".exe";
        }
        command.add(javaExe);
        
        // Build classpath from resolved file paths
        String classpath = buildClasspath(cache.getResolvedFilePaths());
        command.add("-cp");
        command.add(classpath);
        
        // Find JavaFX module path
        String fxModulePath = findJavaFxModulePath(cache.getResolvedFilePaths());
        
        // Add JVM arguments
        for (String arg : manifest.getJvmArgs()) {
            if (arg.contains(FX_MODULE_PATH_PLACEHOLDER)) {
                command.add(arg.replace(FX_MODULE_PATH_PLACEHOLDER, fxModulePath));
            } else {
                command.add(arg);
            }
        }
        
        // Main class
        command.add(manifest.getMainClass());
        
        log.info("Launch command: {}", String.join(" ", command));
        
        // Launch the game
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        pb.start();
        
        log.info("Game launched successfully");
    }
    
    /**
     * Builds the classpath string from resolved file paths.
     */
    private String buildClasspath(List<String> filePaths) {
        String separator = System.getProperty("path.separator");
        return String.join(separator, filePaths);
    }
    
    /**
     * Finds the JavaFX module path from resolved dependencies.
     * Looks for JavaFX jar files and returns their parent directory.
     */
    private String findJavaFxModulePath(List<String> filePaths) {
        for (String path : filePaths) {
            if (path.contains("javafx-controls") || path.contains("javafx-graphics") || path.contains("javafx-base")) {
                File file = new File(path);
                return file.getParent();
            }
        }
        
        // If not found, throw an exception with a clear message
        throw new RuntimeException("JavaFX module path not found in resolved dependencies. Please ensure JavaFX dependencies are present.");
    }
}
