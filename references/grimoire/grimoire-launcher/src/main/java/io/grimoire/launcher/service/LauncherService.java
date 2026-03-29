package io.grimoire.launcher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grimoire.launcher.model.LocalCache;
import io.grimoire.launcher.model.Manifest;
import io.grimoire.launcher.util.LauncherPaths;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Main service that orchestrates the launcher logic: download manifest, resolve artifacts, launch game.
 */
@Singleton
@Slf4j
public class LauncherService {
    
    private final ManifestDownloader manifestDownloader;
    private final ArtifactResolver artifactResolver;
    private final GameLauncher gameLauncher;
    private final ObjectMapper objectMapper;
    
    private Consumer<String> statusUpdateCallback;
    private Consumer<Double> progressUpdateCallback;
    
    public LauncherService(ManifestDownloader manifestDownloader,
                          ArtifactResolver artifactResolver,
                          GameLauncher gameLauncher,
                          ObjectMapper objectMapper) {
        this.manifestDownloader = manifestDownloader;
        this.artifactResolver = artifactResolver;
        this.gameLauncher = gameLauncher;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Sets the callback for status updates.
     */
    public void setStatusUpdateCallback(Consumer<String> callback) {
        this.statusUpdateCallback = callback;
    }
    
    /**
     * Sets the callback for progress updates.
     */
    public void setProgressUpdateCallback(Consumer<Double> callback) {
        this.progressUpdateCallback = callback;
    }
    
    /**
     * Starts the update and launch process.
     */
    public void startUpdateProcess() {
        // Run in background thread
        Thread thread = new Thread(() -> {
            try {
                // Check Java version
                checkJavaVersion();
                
                updateStatus("Checking for updates...");
                updateProgress(0.1);
                
                // Download remote manifest
                Manifest remoteManifest = manifestDownloader.downloadManifest();
                updateProgress(0.2);
                
                // Load local cache if exists
                Path localCachePath = LauncherPaths.getLocalCacheFile();
                LocalCache localCache = loadLocalCache(localCachePath);
                
                // Compare versions
                boolean needsUpdate = true;
                if (localCache != null && localCache.getManifest() != null) {
                    String localVersion = localCache.getManifest().getVersion();
                    String remoteVersion = remoteManifest.getVersion();
                    log.info("Local version: {}, Remote version: {}", localVersion, remoteVersion);
                    
                    if (localVersion.equals(remoteVersion)) {
                        needsUpdate = false;
                        updateStatus("Game is up to date. Launching...");
                        updateProgress(0.9);
                    }
                }
                
                LocalCache cacheToUse;
                if (needsUpdate) {
                    updateStatus("Downloading update: " + remoteManifest.getVersion());
                    updateProgress(0.3);
                    
                    // Resolve artifacts
                    cacheToUse = artifactResolver.resolveArtifacts(remoteManifest);
                    updateProgress(0.7);
                    
                    // Save cache
                    saveLocalCache(localCachePath, cacheToUse);
                    updateProgress(0.8);
                    
                    updateStatus("Update complete. Launching...");
                } else {
                    cacheToUse = localCache;
                }
                
                updateProgress(0.9);
                
                // Launch game
                gameLauncher.launchGame(cacheToUse);
                updateProgress(1.0);
                
                updateStatus("Game launched successfully!");
                
                // Wait a moment before closing
                Thread.sleep(2000);
                
                // Exit launcher
                Platform.exit();
                System.exit(0);
                
            } catch (Exception e) {
                log.error("Error during update process", e);
                updateStatus("Error: " + e.getMessage());
                updateProgress(0.0);
            }
        });
        
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Checks if the user has Java 21 or higher.
     */
    private void checkJavaVersion() {
        String version = System.getProperty("java.version");
        log.info("Java version: {}", version);
        
        // Parse major version
        int majorVersion;
        if (version.startsWith("1.")) {
            majorVersion = Integer.parseInt(version.substring(2, 3));
        } else {
            int dotIndex = version.indexOf('.');
            if (dotIndex != -1) {
                majorVersion = Integer.parseInt(version.substring(0, dotIndex));
            } else {
                majorVersion = Integer.parseInt(version);
            }
        }
        
        if (majorVersion < 21) {
            String message = "Java 21 or higher is required. You have Java " + majorVersion + 
                    ". Please download Java 21 from https://adoptium.net/";
            log.error(message);
            updateStatus(message);
            throw new RuntimeException(message);
        }
    }
    
    /**
     * Loads the local cache from file.
     */
    private LocalCache loadLocalCache(Path path) {
        try {
            if (Files.exists(path)) {
                return objectMapper.readValue(path.toFile(), LocalCache.class);
            }
        } catch (IOException e) {
            log.warn("Failed to load local cache", e);
        }
        return null;
    }
    
    /**
     * Saves the local cache to file.
     */
    private void saveLocalCache(Path path, LocalCache cache) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), cache);
        log.info("Saved local cache to: {}", path);
    }
    
    /**
     * Updates the status message on the UI thread.
     */
    private void updateStatus(String message) {
        log.info("Status: {}", message);
        if (statusUpdateCallback != null) {
            Platform.runLater(() -> statusUpdateCallback.accept(message));
        }
    }
    
    /**
     * Updates the progress on the UI thread.
     */
    private void updateProgress(double progress) {
        if (progressUpdateCallback != null) {
            Platform.runLater(() -> progressUpdateCallback.accept(progress));
        }
    }
}
