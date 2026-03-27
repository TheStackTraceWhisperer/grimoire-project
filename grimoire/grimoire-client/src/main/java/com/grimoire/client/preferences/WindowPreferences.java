package com.grimoire.client.preferences;

import jakarta.inject.Singleton;

import java.util.prefs.Preferences;

/**
 * Service for managing window-related user preferences using Java's built-in Preferences API.
 */
@Singleton
public class WindowPreferences {
    
    private static final String WINDOW_MAXIMIZED_KEY = "window.maximized";
    private static final boolean DEFAULT_MAXIMIZED = false;
    
    private final Preferences preferences;
    
    public WindowPreferences() {
        // Use user preferences for the JavaFX application
        this.preferences = Preferences.userNodeForPackage(WindowPreferences.class);
    }
    
    /**
     * Save the window maximized state.
     * @param maximized true if the window is maximized, false otherwise
     */
    public void saveMaximizedState(boolean maximized) {
        preferences.putBoolean(WINDOW_MAXIMIZED_KEY, maximized);
    }
    
    /**
     * Get the saved window maximized state.
     * @return true if the window should be maximized, false otherwise
     */
    public boolean isMaximized() {
        return preferences.getBoolean(WINDOW_MAXIMIZED_KEY, DEFAULT_MAXIMIZED);
    }
}
