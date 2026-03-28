package com.grimoire.server.system;

import com.grimoire.data.Character;
import com.grimoire.data.CharacterRepository;
import com.grimoire.server.component.*;
import com.grimoire.server.config.GameConfig;
import com.grimoire.server.config.TestGameConfig;
import com.grimoire.ecs.ComponentManager;
import com.grimoire.ecs.EcsWorld;
import com.grimoire.ecs.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for PersistenceSystem.
 */
class PersistenceSystemTest {
    
    /** Test save interval in seconds (1 second = 20 ticks at 20 TPS) */
    private static final int TEST_SAVE_INTERVAL_SECONDS = 1;
    
    /** Number of ticks to trigger a save based on test interval */
    private static final int TICKS_TO_TRIGGER_SAVE = 20;
    
    private EcsWorld ecsWorld;
    private CharacterRepository characterRepository;
    private PersistenceSystem persistenceSystem;
    private GameConfig gameConfig;
    
    @BeforeEach
    void setUp() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        ecsWorld = new EcsWorld(entityManager, componentManager);
        characterRepository = Mockito.mock(CharacterRepository.class);
        
        // Create config with short save interval for testing
        gameConfig = TestGameConfig.create();
        gameConfig.setAutoSaveIntervalSeconds(TEST_SAVE_INTERVAL_SECONDS);
        
        persistenceSystem = new PersistenceSystem(ecsWorld, characterRepository, gameConfig);
    }
    
    @Test
    void testNoSaveOnFirstTick() {
        // Create a persistent entity
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Persistent("1"));
        ecsWorld.addComponent(entityId, new Position(100, 200));
        ecsWorld.addComponent(entityId, new Zone("zone1"));
        
        // First tick should not trigger save
        persistenceSystem.tick(0.05f);
        
        verify(characterRepository, never()).findById(any());
    }
    
    @Test
    void testSaveTriggersAfterInterval() {
        // Create a persistent entity
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Persistent("1"));
        ecsWorld.addComponent(entityId, new Position(100, 200));
        ecsWorld.addComponent(entityId, new Zone("zone1"));
        ecsWorld.addComponent(entityId, new Stats(80, 100, 5, 10));
        ecsWorld.addComponent(entityId, new Experience(50, 100));
        
        // Mock character repository
        Character mockCharacter = Mockito.mock(Character.class);
        when(characterRepository.findById(1L)).thenReturn(Optional.of(mockCharacter));
        
        // Tick enough times to trigger save (20 ticks for 1 second interval at 20 TPS)
        for (int i = 0; i < TICKS_TO_TRIGGER_SAVE; i++) {
            persistenceSystem.tick(0.05f);
        }
        
        // Should have tried to save
        verify(characterRepository, atLeastOnce()).findById(1L);
        verify(characterRepository, atLeastOnce()).update(mockCharacter);
    }
    
    @Test
    void testSaveUpdatesPosition() {
        // Create a persistent entity
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Persistent("1"));
        ecsWorld.addComponent(entityId, new Position(150, 250));
        ecsWorld.addComponent(entityId, new Zone("zone2"));
        
        // Mock character repository
        Character mockCharacter = Mockito.mock(Character.class);
        when(characterRepository.findById(1L)).thenReturn(Optional.of(mockCharacter));
        
        // Tick enough times to trigger save
        for (int i = 0; i < TICKS_TO_TRIGGER_SAVE; i++) {
            persistenceSystem.tick(0.05f);
        }
        
        // Verify position was updated
        verify(mockCharacter).setLastX(150.0);
        verify(mockCharacter).setLastY(250.0);
    }
    
    @Test
    void testSaveUpdatesZone() {
        // Create a persistent entity
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Persistent("1"));
        ecsWorld.addComponent(entityId, new Position(100, 100));
        ecsWorld.addComponent(entityId, new Zone("forest"));
        
        // Mock character repository
        Character mockCharacter = Mockito.mock(Character.class);
        when(characterRepository.findById(1L)).thenReturn(Optional.of(mockCharacter));
        
        // Tick enough times to trigger save
        for (int i = 0; i < TICKS_TO_TRIGGER_SAVE; i++) {
            persistenceSystem.tick(0.05f);
        }
        
        // Verify zone was updated
        verify(mockCharacter).setLastZone("forest");
    }
    
    @Test
    void testSaveUpdatesStats() {
        // Create a persistent entity
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Persistent("1"));
        ecsWorld.addComponent(entityId, new Position(100, 100));
        ecsWorld.addComponent(entityId, new Stats(75, 120, 10, 15));
        
        // Mock character repository
        Character mockCharacter = Mockito.mock(Character.class);
        when(characterRepository.findById(1L)).thenReturn(Optional.of(mockCharacter));
        
        // Tick enough times to trigger save
        for (int i = 0; i < TICKS_TO_TRIGGER_SAVE; i++) {
            persistenceSystem.tick(0.05f);
        }
        
        // Verify stats were updated
        verify(mockCharacter).setCurrentHp(75);
        verify(mockCharacter).setMaxHp(120);
    }
    
    @Test
    void testSaveUpdatesExperience() {
        // Create a persistent entity
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Persistent("1"));
        ecsWorld.addComponent(entityId, new Position(100, 100));
        ecsWorld.addComponent(entityId, new Experience(500, 1000));
        
        // Mock character repository
        Character mockCharacter = Mockito.mock(Character.class);
        when(characterRepository.findById(1L)).thenReturn(Optional.of(mockCharacter));
        
        // Tick enough times to trigger save
        for (int i = 0; i < TICKS_TO_TRIGGER_SAVE; i++) {
            persistenceSystem.tick(0.05f);
        }
        
        // Verify experience was updated
        verify(mockCharacter).setCurrentXp(500);
        verify(mockCharacter).setXpToNextLevel(1000);
    }
    
    @Test
    void testNoPersistentEntitiesNoSave() {
        // Create a non-persistent entity
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Position(100, 100));
        
        // Tick enough times to trigger save
        for (int i = 0; i < TICKS_TO_TRIGGER_SAVE; i++) {
            persistenceSystem.tick(0.05f);
        }
        
        // Should not have tried to save
        verify(characterRepository, never()).findById(any());
    }
    
    @Test
    void testInvalidCharacterIdHandled() {
        // Create a persistent entity with invalid character ID
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Persistent("invalid"));
        ecsWorld.addComponent(entityId, new Position(100, 100));
        
        // Tick enough times to trigger save - should not throw
        for (int i = 0; i < TICKS_TO_TRIGGER_SAVE; i++) {
            persistenceSystem.tick(0.05f);
        }
        
        // Should not have tried to find character
        verify(characterRepository, never()).findById(any());
    }
    
    @Test
    void testCharacterNotFoundHandled() {
        // Create a persistent entity
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Persistent("999"));
        ecsWorld.addComponent(entityId, new Position(100, 100));
        
        // Mock character not found
        when(characterRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Tick enough times to trigger save - should not throw
        for (int i = 0; i < TICKS_TO_TRIGGER_SAVE; i++) {
            persistenceSystem.tick(0.05f);
        }
        
        // Should have tried to find but not update
        verify(characterRepository, atLeastOnce()).findById(999L);
        verify(characterRepository, never()).update(any());
    }
    
    @Test
    void testMultiplePersistentEntitiesSaved() {
        // Create multiple persistent entities
        String entityId1 = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId1, new Persistent("1"));
        ecsWorld.addComponent(entityId1, new Position(100, 100));
        
        String entityId2 = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId2, new Persistent("2"));
        ecsWorld.addComponent(entityId2, new Position(200, 200));
        
        // Mock characters
        Character mockCharacter1 = Mockito.mock(Character.class);
        Character mockCharacter2 = Mockito.mock(Character.class);
        when(characterRepository.findById(1L)).thenReturn(Optional.of(mockCharacter1));
        when(characterRepository.findById(2L)).thenReturn(Optional.of(mockCharacter2));
        
        // Tick enough times to trigger save
        for (int i = 0; i < TICKS_TO_TRIGGER_SAVE; i++) {
            persistenceSystem.tick(0.05f);
        }
        
        // Both should be saved
        verify(characterRepository, atLeastOnce()).findById(1L);
        verify(characterRepository, atLeastOnce()).findById(2L);
        verify(characterRepository, atLeastOnce()).update(mockCharacter1);
        verify(characterRepository, atLeastOnce()).update(mockCharacter2);
    }
    
    @Test
    void testSaveAllImmediately() {
        // Create a persistent entity
        String entityId = ecsWorld.createEntity();
        ecsWorld.addComponent(entityId, new Persistent("1"));
        ecsWorld.addComponent(entityId, new Position(100, 200));
        
        // Mock character repository
        Character mockCharacter = Mockito.mock(Character.class);
        when(characterRepository.findById(1L)).thenReturn(Optional.of(mockCharacter));
        
        // Call saveAllImmediately
        persistenceSystem.saveAllImmediately();
        
        // Should have saved immediately without waiting for ticks
        verify(characterRepository, atLeastOnce()).findById(1L);
        verify(characterRepository, atLeastOnce()).update(mockCharacter);
    }
}
