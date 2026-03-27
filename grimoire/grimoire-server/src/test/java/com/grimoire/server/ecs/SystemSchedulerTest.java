package com.grimoire.server.ecs;

import com.grimoire.data.CharacterRepository;
import com.grimoire.server.config.GameConfig;
import com.grimoire.server.config.TestGameConfig;
import com.grimoire.server.system.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SystemSchedulerTest {
    
    @Test
    void testSystemSchedulerTickExecutesAllSystems() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        EcsWorld ecsWorld = new EcsWorld(entityManager, componentManager);
        
        // Create mock systems
        PlayerInputSystem mockPlayerInput = Mockito.mock(PlayerInputSystem.class);
        PortalCooldownSystem mockPortalCooldown = Mockito.mock(PortalCooldownSystem.class);
        NpcAiSystem mockNpcAi = Mockito.mock(NpcAiSystem.class);
        SpatialGridSystem mockSpatialGrid = Mockito.mock(SpatialGridSystem.class);
        MovementSystem mockMovement = Mockito.mock(MovementSystem.class);
        CombatSystem mockCombat = Mockito.mock(CombatSystem.class);
        LevelUpSystem mockLevelUp = Mockito.mock(LevelUpSystem.class);
        ZoneChangeSystem mockZoneChange = Mockito.mock(ZoneChangeSystem.class);
        NetworkSyncSystem mockNetworkSync = Mockito.mock(NetworkSyncSystem.class);
        NetworkVisibilitySystem mockNetworkVisibility = Mockito.mock(NetworkVisibilitySystem.class);
        PersistenceSystem mockPersistence = Mockito.mock(PersistenceSystem.class);
        
        SystemScheduler scheduler = new SystemScheduler(
            ecsWorld,
            mockPlayerInput,
            mockPortalCooldown,
            mockNpcAi,
            mockSpatialGrid,
            mockMovement,
            mockCombat,
            mockLevelUp,
            mockZoneChange,
            mockNetworkSync,
            mockNetworkVisibility,
            mockPersistence
        );
        
        long initialTick = ecsWorld.getCurrentTick();
        
        scheduler.tick(0.05f);
        
        // Verify all systems were called
        verify(mockPlayerInput, times(1)).tick(0.05f);
        verify(mockPortalCooldown, times(1)).tick(0.05f);
        verify(mockNpcAi, times(1)).tick(0.05f);
        verify(mockSpatialGrid, times(1)).tick(0.05f);
        verify(mockMovement, times(1)).tick(0.05f);
        verify(mockCombat, times(1)).tick(0.05f);
        verify(mockLevelUp, times(1)).tick(0.05f);
        verify(mockZoneChange, times(1)).tick(0.05f);
        verify(mockNetworkSync, times(1)).tick(0.05f);
        verify(mockNetworkVisibility, times(1)).tick(0.05f);
        verify(mockPersistence, times(1)).tick(0.05f);
        
        // Verify tick was incremented
        assertEquals(initialTick + 1, ecsWorld.getCurrentTick());
    }
    
    @Test
    void testSystemSchedulerIncrementsTick() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        EcsWorld ecsWorld = new EcsWorld(entityManager, componentManager);
        GameConfig gameConfig = TestGameConfig.create();
        
        // Use real systems for this test
        SpatialGridSystem spatialGrid = new SpatialGridSystem(ecsWorld, gameConfig);
        PlayerInputSystem playerInput = new PlayerInputSystem(ecsWorld, gameConfig);
        PortalCooldownSystem portalCooldown = new PortalCooldownSystem(ecsWorld);
        NpcAiSystem npcAi = new NpcAiSystem(ecsWorld, spatialGrid, gameConfig);
        MovementSystem movement = new MovementSystem(ecsWorld, spatialGrid);
        CombatSystem combat = new CombatSystem(ecsWorld, spatialGrid, gameConfig);
        LevelUpSystem levelUp = new LevelUpSystem(ecsWorld);
        ZoneChangeSystem zoneChange = new ZoneChangeSystem(ecsWorld);
        NetworkSyncSystem networkSync = new NetworkSyncSystem(ecsWorld);
        NetworkVisibilitySystem networkVisibility = new NetworkVisibilitySystem(ecsWorld);
        CharacterRepository characterRepository = Mockito.mock(CharacterRepository.class);
        PersistenceSystem persistence = new PersistenceSystem(ecsWorld, characterRepository, gameConfig);
        
        SystemScheduler scheduler = new SystemScheduler(
            ecsWorld,
            playerInput,
            portalCooldown,
            npcAi,
            spatialGrid,
            movement,
            combat,
            levelUp,
            zoneChange,
            networkSync,
            networkVisibility,
            persistence
        );
        
        assertEquals(0, ecsWorld.getCurrentTick());
        
        scheduler.tick(0.05f);
        assertEquals(1, ecsWorld.getCurrentTick());
        
        scheduler.tick(0.05f);
        assertEquals(2, ecsWorld.getCurrentTick());
    }
}
