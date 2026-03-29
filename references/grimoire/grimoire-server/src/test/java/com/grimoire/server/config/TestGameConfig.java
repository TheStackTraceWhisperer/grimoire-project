package com.grimoire.server.config;

/**
 * Test utility for creating GameConfig instances with default values.
 */
public final class TestGameConfig {
    
    private TestGameConfig() {
        // Utility class
    }
    
    /**
     * Creates a GameConfig with default test values.
     * @return a new GameConfig instance
     */
    public static GameConfig create() {
        GameConfig config = new GameConfig();
        config.setAttackRange(50.0);
        config.setPlayerSpeed(5.0);
        config.setSpatialGridCellSize(64);
        config.setSessionValidityMinutes(60);
        config.setAutoSaveIntervalSeconds(300);
        config.setNpcAggroRange(100.0);
        config.setNpcSpeed(3.0);
        config.setAttackCooldownTicks(20);
        config.setNpcLeashRadius(200.0);
        return config;
    }
}
