package com.grimoire.server.system;
import io.micronaut.core.annotation.Order;

import com.grimoire.data.Character;
import com.grimoire.data.CharacterRepository;
import com.grimoire.server.component.Experience;
import com.grimoire.server.component.Persistent;
import com.grimoire.server.component.Position;
import com.grimoire.server.component.Stats;
import com.grimoire.server.component.Zone;
import com.grimoire.server.config.GameConfig;
import com.grimoire.ecs.EcsWorld;
import com.grimoire.ecs.GameSystem;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Periodically saves the state of all persistent entities to the database.
 * 
 * <p>This system runs as part of the game loop and saves player state at a configurable
 * interval (default: 5 minutes). This prevents data loss in case of server crashes,
 * OOM errors, or other unexpected failures.</p>
 * 
 * <p>Persistence interval is controlled by {@code game.tuning.autoSaveIntervalSeconds}
 * in application.yml.</p>
 */
@Order(1100)
@Singleton
@Slf4j
public class PersistenceSystem implements GameSystem {
    
    private static final float TICKS_PER_SECOND = 20.0f;
    
    private final EcsWorld ecsWorld;
    private final CharacterRepository characterRepository;
    private final int saveIntervalTicks;
    
    private int ticksSinceLastSave = 0;
    
    public PersistenceSystem(EcsWorld ecsWorld, CharacterRepository characterRepository, GameConfig gameConfig) {
        this.ecsWorld = ecsWorld;
        this.characterRepository = characterRepository;
        this.saveIntervalTicks = (int) (gameConfig.getAutoSaveIntervalSeconds() * TICKS_PER_SECOND);
        log.info("PersistenceSystem initialized with auto-save interval of {} seconds ({} ticks)", 
                 gameConfig.getAutoSaveIntervalSeconds(), saveIntervalTicks);
    }
    
    @Override
    public void tick(float deltaTime) {
        ticksSinceLastSave++;
        
        if (ticksSinceLastSave >= saveIntervalTicks) {
            saveAllPersistentEntities();
            ticksSinceLastSave = 0;
        }
    }
    
    /**
     * Saves the state of all entities with a Persistent component to the database.
     * This is called periodically based on the configured interval.
     */
    private void saveAllPersistentEntities() {
        List<String> persistentEntities = new ArrayList<>();
        for (String entityId : ecsWorld.getEntitiesWithComponent(Persistent.class)) {
            persistentEntities.add(entityId);
        }
        
        if (persistentEntities.isEmpty()) {
            return;
        }
        
        int savedCount = 0;
        for (String entityId : persistentEntities) {
            if (saveEntityState(entityId)) {
                savedCount++;
            }
        }
        
        if (savedCount > 0) {
            log.info("Auto-save completed: {} entities saved", savedCount);
        }
    }
    
    /**
     * Saves a single entity's state to the database.
     * 
     * @param entityId the entity to save
     * @return true if saved successfully, false otherwise
     */
    private boolean saveEntityState(String entityId) {
        Optional<Persistent> persistentOpt = ecsWorld.getComponent(entityId, Persistent.class);
        if (persistentOpt.isEmpty()) {
            return false;
        }
        
        try {
            // Note: Persistent.accountId() actually stores the character ID (naming issue in the original component)
            Long characterId = Long.parseLong(persistentOpt.get().accountId());
            Optional<Character> characterOpt = characterRepository.findById(characterId);
            
            if (characterOpt.isEmpty()) {
                log.warn("Character {} not found during auto-save", characterId);
                return false;
            }
            
            Character character = characterOpt.get();
            
            // Save position
            ecsWorld.getComponent(entityId, Position.class).ifPresent(pos -> {
                character.setLastX(pos.x());
                character.setLastY(pos.y());
            });
            
            // Save zone
            ecsWorld.getComponent(entityId, Zone.class).ifPresent(zone ->
                character.setLastZone(zone.zoneId()));
            
            // Save HP
            ecsWorld.getComponent(entityId, Stats.class).ifPresent(stats -> {
                character.setCurrentHp(stats.hp());
                character.setMaxHp(stats.maxHp());
            });
            
            // Save Experience
            ecsWorld.getComponent(entityId, Experience.class).ifPresent(exp -> {
                character.setCurrentXp(exp.currentXp());
                character.setXpToNextLevel(exp.xpToNextLevel());
            });
            
            character.setLastPlayedAt(LocalDateTime.now());
            characterRepository.update(character);
            
            return true;
        } catch (NumberFormatException e) {
            log.warn("Invalid character ID in persistent component for entity {}: {}", 
                     entityId, persistentOpt.get().accountId());
            return false;
        }
    }
    
    /**
     * Forces an immediate save of all persistent entities.
     * Useful for graceful shutdown scenarios.
     */
    public void saveAllImmediately() {
        log.info("Forcing immediate save of all persistent entities");
        saveAllPersistentEntities();
    }
}
