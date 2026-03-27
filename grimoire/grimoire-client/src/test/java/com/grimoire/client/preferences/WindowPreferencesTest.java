package com.grimoire.client.preferences;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

class WindowPreferencesTest {
    
    private WindowPreferences windowPreferences;
    private Preferences testPrefs;
    
    @BeforeEach
    void setUp() {
        windowPreferences = new WindowPreferences();
        testPrefs = Preferences.userNodeForPackage(WindowPreferences.class);
    }
    
    @AfterEach
    void tearDown() throws BackingStoreException {
        // Clean up preferences after each test
        testPrefs.clear();
    }
    
    @Test
    void testDefaultMaximizedStateIsFalse() {
        // When no preference is saved, should return false
        assertFalse(windowPreferences.isMaximized());
    }
    
    @Test
    void testSaveMaximizedStateTrue() {
        windowPreferences.saveMaximizedState(true);
        assertTrue(windowPreferences.isMaximized());
    }
    
    @Test
    void testSaveMaximizedStateFalse() {
        windowPreferences.saveMaximizedState(false);
        assertFalse(windowPreferences.isMaximized());
    }
    
    @Test
    void testMaximizedStatePersistsAcrossInstances() {
        // Save state with one instance
        windowPreferences.saveMaximizedState(true);
        
        // Create new instance and verify state persists
        WindowPreferences newInstance = new WindowPreferences();
        assertTrue(newInstance.isMaximized());
    }
    
    @Test
    void testToggleMaximizedState() {
        // Initially false
        assertFalse(windowPreferences.isMaximized());
        
        // Set to true
        windowPreferences.saveMaximizedState(true);
        assertTrue(windowPreferences.isMaximized());
        
        // Set back to false
        windowPreferences.saveMaximizedState(false);
        assertFalse(windowPreferences.isMaximized());
    }
    
    @Test
    void testMultipleSaveOperations() {
        // Save multiple times with different values
        windowPreferences.saveMaximizedState(true);
        windowPreferences.saveMaximizedState(false);
        windowPreferences.saveMaximizedState(true);
        
        // Should retain the last value
        assertTrue(windowPreferences.isMaximized());
    }
}
