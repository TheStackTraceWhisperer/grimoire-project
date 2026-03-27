package io.grimoire.launcher.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for managing launcher file paths and directories.
 */
public class LauncherPaths {
    
    private static final String APP_NAME = "Grimoire";
    
    /**
     * Gets the base directory for the launcher data.
     * On Windows: %APPDATA%/Grimoire
     * On Mac: ~/Library/Application Support/Grimoire
     * On Linux: ~/.grimoire
     */
    public static Path getBaseDirectory() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        Path baseDir;
        
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData == null) {
                appData = System.getProperty("user.home") + "\\AppData\\Roaming";
            }
            baseDir = Paths.get(appData, APP_NAME);
        } else if (os.contains("mac")) {
            baseDir = Paths.get(System.getProperty("user.home"), "Library", "Application Support", APP_NAME);
        } else {
            // Linux and other Unix-like systems
            baseDir = Paths.get(System.getProperty("user.home"), "." + APP_NAME.toLowerCase());
        }
        
        // Create directory if it doesn't exist
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }
        
        return baseDir;
    }
    
    /**
     * Gets the cache directory for downloaded artifacts.
     */
    public static Path getCacheDirectory() throws IOException {
        Path cacheDir = getBaseDirectory().resolve("cache");
        if (!Files.exists(cacheDir)) {
            Files.createDirectories(cacheDir);
        }
        return cacheDir;
    }
    
    /**
     * Gets the path to the local cache JSON file.
     */
    public static Path getLocalCacheFile() throws IOException {
        return getBaseDirectory().resolve("local-cache.json");
    }
}
