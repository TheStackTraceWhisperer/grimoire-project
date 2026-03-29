package com.grimoire.server.ecs;

import com.grimoire.server.system.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Schedules and executes all game systems in a fixed order.
 */
@Singleton
public class SystemScheduler {
    
    private final EcsWorld ecsWorld;
    private final List<GameSystem> systems;
    
    @Inject
    public SystemScheduler(
            EcsWorld ecsWorld,
            PlayerInputSystem playerInputSystem,
            PortalCooldownSystem portalCooldownSystem,
            NpcAiSystem npcAiSystem,
            SpatialGridSystem spatialGridSystem,
            MovementSystem movementSystem,
            CombatSystem combatSystem,
            LevelUpSystem levelUpSystem,
            ZoneChangeSystem zoneChangeSystem,
            NetworkSyncSystem networkSyncSystem,
            NetworkVisibilitySystem networkVisibilitySystem,
            PersistenceSystem persistenceSystem
    ) {
        this.ecsWorld = ecsWorld;
        // Systems are executed in this strict order
        this.systems = List.of(
                playerInputSystem,
                portalCooldownSystem,
                npcAiSystem,
                spatialGridSystem,  // Update spatial grid before movement
                movementSystem,
                combatSystem,
                levelUpSystem,  // Process level ups after combat (XP gain)
                zoneChangeSystem,
                networkSyncSystem,
                networkVisibilitySystem,
                persistenceSystem  // Periodic auto-save at the end of each tick
        );
    }
    
    /**
     * Executes all systems in order.
     * @param deltaTime time elapsed since last tick
     */
    public void tick(float deltaTime) {
        for (GameSystem system : systems) {
            system.tick(deltaTime);
        }
        ecsWorld.incrementTick();
    }
}
