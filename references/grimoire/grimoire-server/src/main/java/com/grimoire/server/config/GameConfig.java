package com.grimoire.server.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for game-specific settings.
 * 
 * <p>Consolidates magic numbers and tunable game parameters in application.yml
 * to enable tuning without recompilation.</p>
 */
@ConfigurationProperties("game.tuning")
@Getter
@Setter
public class GameConfig {
    
    /**
     * Default attack range in world units. Default is 50.0.
     */
    private double attackRange = 50.0;
    
    /**
     * Player movement speed in units per second. Default is 5.0.
     */
    private double playerSpeed = 5.0;
    
    /**
     * Spatial grid cell size in world units. Default is 64.
     */
    private int spatialGridCellSize = 64;
    
    /**
     * Session validity duration in minutes. Default is 60.
     */
    private int sessionValidityMinutes = 60;
    
    /**
     * Auto-save interval in seconds for persistent entities. Default is 300 (5 minutes).
     */
    private int autoSaveIntervalSeconds = 300;
    
    /**
     * NPC aggro range in world units. Default is 100.0.
     */
    private double npcAggroRange = 100.0;
    
    /**
     * NPC movement speed in units per second. Default is 3.0.
     */
    private double npcSpeed = 3.0;
    
    /**
     * Attack cooldown in ticks between attacks. Default is 20 (1 second at 20 TPS).
     */
    private int attackCooldownTicks = 20;
    
    /**
     * Default leash radius for NPCs in world units. Default is 200.0.
     */
    private double npcLeashRadius = 200.0;
}
