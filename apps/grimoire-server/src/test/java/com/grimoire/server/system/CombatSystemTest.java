package com.grimoire.server.system;

import com.grimoire.server.component.AttackIntent;
import com.grimoire.server.component.BoundingBox;
import com.grimoire.server.component.Dead;
import com.grimoire.server.component.Dirty;
import com.grimoire.server.component.Experience;
import com.grimoire.server.component.Monster;
import com.grimoire.server.component.PlayerConnection;
import com.grimoire.server.component.Position;
import com.grimoire.server.component.Solid;
import com.grimoire.server.component.Stats;
import com.grimoire.server.component.Zone;
import com.grimoire.ecs.ComponentManager;
import com.grimoire.ecs.EcsWorld;
import com.grimoire.ecs.EntityManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CombatSystemTest {
    
    private EcsWorld ecsWorld;
    private SpatialGridSystem spatialGridSystem;
    private CombatSystem combatSystem;
    
    @BeforeEach
    void setUp() {
        EntityManager entityManager = new EntityManager();
        ComponentManager componentManager = new ComponentManager();
        ecsWorld = new EcsWorld(entityManager, componentManager);
        com.grimoire.server.config.GameConfig gameConfig = com.grimoire.server.config.TestGameConfig.create();
        spatialGridSystem = new SpatialGridSystem(ecsWorld, gameConfig);
        combatSystem = new CombatSystem(ecsWorld, spatialGridSystem, gameConfig);
    }
    
    @Test
    void testAttackDealsDamage() {
        // Create attacker with attack=15
        String attackerId = ecsWorld.createEntity();
        ecsWorld.addComponent(attackerId, new Stats(100, 100, 5, 15));
        ecsWorld.addComponent(attackerId, new Position(0, 0));
        
        // Create target with defense=5, so damage should be 15 - 5 = 10
        String targetId = ecsWorld.createEntity();
        ecsWorld.addComponent(targetId, new Stats(50, 100, 5, 10));
        ecsWorld.addComponent(targetId, new Position(10, 10)); // In range (< 50)
        
        // Add attack intent
        ecsWorld.addComponent(attackerId, new AttackIntent(targetId));
        
        // Process combat
        combatSystem.tick(0.05f);
        
        // Verify damage was applied (50 - 10 = 40)
        Stats targetStats = ecsWorld.getComponent(targetId, Stats.class).orElseThrow();
        assertEquals(40, targetStats.hp());
        
        // Verify attack intent was removed
        assertFalse(ecsWorld.hasComponent(attackerId, AttackIntent.class));
    }
    
    @Test
    void testMinimumDamageIsOne() {
        // Create attacker with low attack
        String attackerId = ecsWorld.createEntity();
        ecsWorld.addComponent(attackerId, new Stats(100, 100, 5, 5));
        ecsWorld.addComponent(attackerId, new Position(0, 0));
        
        // Create target with high defense (attack - defense = negative, should still do 1)
        String targetId = ecsWorld.createEntity();
        ecsWorld.addComponent(targetId, new Stats(100, 100, 20, 10));
        ecsWorld.addComponent(targetId, new Position(10, 10));
        
        // Add attack intent
        ecsWorld.addComponent(attackerId, new AttackIntent(targetId));
        
        // Process combat
        combatSystem.tick(0.05f);
        
        // Verify minimum 1 damage was applied
        Stats targetStats = ecsWorld.getComponent(targetId, Stats.class).orElseThrow();
        assertEquals(99, targetStats.hp());
    }
    
    @Test
    void testAttackOutOfRangeDoesNothing() {
        // Create attacker
        String attackerId = ecsWorld.createEntity();
        ecsWorld.addComponent(attackerId, new Stats(100, 100, 5, 15));
        ecsWorld.addComponent(attackerId, new Position(0, 0));
        
        // Create target far away (> 50 units)
        String targetId = ecsWorld.createEntity();
        ecsWorld.addComponent(targetId, new Stats(50, 100, 5, 10));
        ecsWorld.addComponent(targetId, new Position(100, 100));
        
        // Add attack intent
        ecsWorld.addComponent(attackerId, new AttackIntent(targetId));
        
        // Process combat
        combatSystem.tick(0.05f);
        
        // Verify no damage was applied
        Stats targetStats = ecsWorld.getComponent(targetId, Stats.class).orElseThrow();
        assertEquals(50, targetStats.hp());
    }
    
    @Test
    void testKillingTargetMarksAsDead() {
        // Create attacker with high attack
        String attackerId = ecsWorld.createEntity();
        ecsWorld.addComponent(attackerId, new Stats(100, 100, 5, 100));
        ecsWorld.addComponent(attackerId, new Position(0, 0));
        
        // Create weak target
        String targetId = ecsWorld.createEntity();
        ecsWorld.addComponent(targetId, new Stats(10, 100, 0, 10));
        ecsWorld.addComponent(targetId, new Position(10, 10));
        
        // Add attack intent
        ecsWorld.addComponent(attackerId, new AttackIntent(targetId));
        
        // Process combat
        combatSystem.tick(0.05f);
        
        // Target should be marked as dead and destroyed
        assertFalse(ecsWorld.entityExists(targetId));
    }
    
    @Test
    void testAttackNonExistentTargetDoesNothing() {
        // Create attacker
        String attackerId = ecsWorld.createEntity();
        ecsWorld.addComponent(attackerId, new Stats(100, 100, 5, 15));
        ecsWorld.addComponent(attackerId, new Position(0, 0));
        
        // Add attack intent for non-existent target
        ecsWorld.addComponent(attackerId, new AttackIntent("non-existent-entity"));
        
        // Process combat - should not throw
        combatSystem.tick(0.05f);
        
        // Verify attack intent was removed
        assertFalse(ecsWorld.hasComponent(attackerId, AttackIntent.class));
    }
    
    @Test
    void testAttackDeadTargetDoesNothing() {
        // Create attacker
        String attackerId = ecsWorld.createEntity();
        ecsWorld.addComponent(attackerId, new Stats(100, 100, 5, 15));
        ecsWorld.addComponent(attackerId, new Position(0, 0));
        
        // Create target that is already dead
        String targetId = ecsWorld.createEntity();
        ecsWorld.addComponent(targetId, new Stats(0, 100, 5, 10));
        ecsWorld.addComponent(targetId, new Position(10, 10));
        ecsWorld.addComponent(targetId, new Dead(null));
        
        // Add attack intent
        ecsWorld.addComponent(attackerId, new AttackIntent(targetId));
        
        // Process combat (will also process death)
        combatSystem.tick(0.05f);
        
        // Target should have been destroyed during death processing
        assertFalse(ecsWorld.entityExists(targetId));
    }
    
    @Test
    void testDeathNotifiesPlayersInSameZone() {
        // Create a player connection mock
        Channel mockChannel = Mockito.mock(Channel.class);
        ChannelFuture mockFuture = Mockito.mock(ChannelFuture.class);
        when(mockChannel.writeAndFlush(any())).thenReturn(mockFuture);
        
        // Create a player in the same zone
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone1"));
        ecsWorld.addComponent(playerId, new Position(0, 0));
        
        // Create a monster in the same zone that dies
        String monsterId = ecsWorld.createEntity();
        ecsWorld.addComponent(monsterId, new Zone("zone1"));
        ecsWorld.addComponent(monsterId, new Position(10, 10));
        ecsWorld.addComponent(monsterId, new Dead(null));
        
        // Process death
        combatSystem.tick(0.05f);
        
        // Verify despawn packet was sent
        verify(mockChannel).writeAndFlush(any());
        
        // Verify monster was destroyed
        assertFalse(ecsWorld.entityExists(monsterId));
    }
    
    @Test
    void testDeathDoesNotNotifyPlayersInDifferentZone() {
        // Create a player connection mock
        Channel mockChannel = Mockito.mock(Channel.class);
        
        // Create a player in different zone
        String playerId = ecsWorld.createEntity();
        ecsWorld.addComponent(playerId, new PlayerConnection(mockChannel));
        ecsWorld.addComponent(playerId, new Zone("zone2"));
        ecsWorld.addComponent(playerId, new Position(0, 0));
        
        // Create a monster in zone1 that dies
        String monsterId = ecsWorld.createEntity();
        ecsWorld.addComponent(monsterId, new Zone("zone1"));
        ecsWorld.addComponent(monsterId, new Position(10, 10));
        ecsWorld.addComponent(monsterId, new Dead(null));
        
        // Process death
        combatSystem.tick(0.05f);
        
        // Verify no despawn packet was sent to player in different zone
        verify(mockChannel, never()).writeAndFlush(any());
    }
    
    @Test
    void testDirtyFlagSetAfterDamage() {
        // Create attacker
        String attackerId = ecsWorld.createEntity();
        ecsWorld.addComponent(attackerId, new Stats(100, 100, 5, 15));
        ecsWorld.addComponent(attackerId, new Position(0, 0));
        
        // Create target
        String targetId = ecsWorld.createEntity();
        ecsWorld.addComponent(targetId, new Stats(50, 100, 5, 10));
        ecsWorld.addComponent(targetId, new Position(10, 10));
        
        // Add attack intent
        ecsWorld.addComponent(attackerId, new AttackIntent(targetId));
        
        // Process combat
        combatSystem.tick(0.05f);
        
        // Verify dirty flag was set on target
        assertTrue(ecsWorld.hasComponent(targetId, Dirty.class));
    }
    
    @Test
    void testKillingMonsterAwardsXp() {
        // Create attacker (player) with Experience component
        String attackerId = ecsWorld.createEntity();
        ecsWorld.addComponent(attackerId, new Stats(100, 100, 5, 100));
        ecsWorld.addComponent(attackerId, new Position(0, 0));
        ecsWorld.addComponent(attackerId, new Experience(50, 100));
        
        // Create monster target with 10 XP reward
        String targetId = ecsWorld.createEntity();
        ecsWorld.addComponent(targetId, new Stats(10, 100, 0, 10));
        ecsWorld.addComponent(targetId, new Position(10, 10));
        ecsWorld.addComponent(targetId, new Monster(Monster.MonsterType.RAT)); // RAT gives 10 XP
        
        // Add attack intent
        ecsWorld.addComponent(attackerId, new AttackIntent(targetId));
        
        // Process combat (monster dies)
        combatSystem.tick(0.05f);
        
        // Monster should be destroyed
        assertFalse(ecsWorld.entityExists(targetId));
        
        // Attacker should have gained XP
        var expOpt = ecsWorld.getComponent(attackerId, Experience.class);
        assertTrue(expOpt.isPresent());
        assertEquals(60, expOpt.get().currentXp()); // 50 + 10
    }
    
    @Test
    void testKillingNonMonsterDoesNotAwardXp() {
        // Create attacker (player) with Experience component
        String attackerId = ecsWorld.createEntity();
        ecsWorld.addComponent(attackerId, new Stats(100, 100, 5, 100));
        ecsWorld.addComponent(attackerId, new Position(0, 0));
        ecsWorld.addComponent(attackerId, new Experience(50, 100));
        
        // Create non-monster target (no Monster component)
        String targetId = ecsWorld.createEntity();
        ecsWorld.addComponent(targetId, new Stats(10, 100, 0, 10));
        ecsWorld.addComponent(targetId, new Position(10, 10));
        
        // Add attack intent
        ecsWorld.addComponent(attackerId, new AttackIntent(targetId));
        
        // Process combat
        combatSystem.tick(0.05f);
        
        // Target should be destroyed
        assertFalse(ecsWorld.entityExists(targetId));
        
        // Attacker XP should remain unchanged (no Monster = no XP)
        var expOpt = ecsWorld.getComponent(attackerId, Experience.class);
        assertTrue(expOpt.isPresent());
        assertEquals(50, expOpt.get().currentXp());
    }
    
    @Test
    void testDeadComponentContainsKillerId() {
        // Create attacker
        String attackerId = ecsWorld.createEntity();
        ecsWorld.addComponent(attackerId, new Stats(100, 100, 5, 100));
        ecsWorld.addComponent(attackerId, new Position(0, 0));
        
        // Create weak target (will be killed in one hit)
        String targetId = ecsWorld.createEntity();
        ecsWorld.addComponent(targetId, new Stats(10, 100, 0, 10));
        ecsWorld.addComponent(targetId, new Position(10, 10));
        
        // Add attack intent
        ecsWorld.addComponent(attackerId, new AttackIntent(targetId));
        
        // Add Experience and Monster components to verify killerId via XP awarding
        // (XP is only awarded if Dead.killerId is correctly set to the attacker)
        ecsWorld.addComponent(attackerId, new Experience(0, 100));
        ecsWorld.addComponent(targetId, new Monster(Monster.MonsterType.WOLF)); // 25 XP
        
        combatSystem.tick(0.05f);
        
        // XP was correctly awarded, proving Dead.killerId was set to attackerId
        var expOpt = ecsWorld.getComponent(attackerId, Experience.class);
        assertTrue(expOpt.isPresent());
        assertEquals(25, expOpt.get().currentXp());
    }
    
    @Test
    void testDeadComponentKillerIdIsSetCorrectly() {
        // Create attacker with high attack to ensure one-hit kill
        String attackerId = ecsWorld.createEntity();
        ecsWorld.addComponent(attackerId, new Stats(100, 100, 5, 100));
        ecsWorld.addComponent(attackerId, new Position(0, 0));
        
        // Create weak target that will die
        String targetId = ecsWorld.createEntity();
        ecsWorld.addComponent(targetId, new Stats(10, 100, 0, 10));
        ecsWorld.addComponent(targetId, new Position(10, 10));
        
        // Set up attack
        ecsWorld.addComponent(attackerId, new AttackIntent(targetId));
        
        // Add Experience and Monster components to verify killerId via XP awarding
        ecsWorld.addComponent(attackerId, new Experience(0, 100));
        ecsWorld.addComponent(targetId, new Monster(Monster.MonsterType.RAT)); // 10 XP
        
        combatSystem.tick(0.05f);
        
        // Target is destroyed after death processing
        assertFalse(ecsWorld.entityExists(targetId));
        
        // Verify killerId was correctly set by checking that XP was awarded to the correct attacker
        var expOpt = ecsWorld.getComponent(attackerId, Experience.class);
        assertTrue(expOpt.isPresent());
        assertEquals(10, expOpt.get().currentXp()); // RAT gives 10 XP
    }
    
    @Test
    void testAttackCooldownPreventsRapidAttacks() {
        // Create attacker
        String attackerId = ecsWorld.createEntity();
        ecsWorld.addComponent(attackerId, new Stats(100, 100, 5, 15));
        ecsWorld.addComponent(attackerId, new Position(0, 0));
        
        // Create target with enough HP to survive multiple hits
        String targetId = ecsWorld.createEntity();
        ecsWorld.addComponent(targetId, new Stats(100, 100, 5, 10));
        ecsWorld.addComponent(targetId, new Position(10, 10));
        
        // First attack
        ecsWorld.addComponent(attackerId, new AttackIntent(targetId));
        combatSystem.tick(0.05f);
        
        // Verify first attack dealt damage
        var statsAfterFirstAttack = ecsWorld.getComponent(targetId, Stats.class);
        assertTrue(statsAfterFirstAttack.isPresent());
        assertEquals(90, statsAfterFirstAttack.get().hp()); // 100 - 10
        
        // Verify attacker now has cooldown
        assertTrue(ecsWorld.hasComponent(attackerId, com.grimoire.server.component.AttackCooldown.class));
        
        // Try to attack again while on cooldown
        ecsWorld.addComponent(attackerId, new AttackIntent(targetId));
        combatSystem.tick(0.05f);
        
        // Attack should be rejected - HP should remain the same
        var statsAfterSecondAttempt = ecsWorld.getComponent(targetId, Stats.class);
        assertTrue(statsAfterSecondAttempt.isPresent());
        assertEquals(90, statsAfterSecondAttempt.get().hp()); // Still 90, attack was rejected
    }
    
    @Test
    void testAttackCooldownDecrementsOverTime() {
        // Create attacker
        String attackerId = ecsWorld.createEntity();
        ecsWorld.addComponent(attackerId, new Stats(100, 100, 5, 15));
        ecsWorld.addComponent(attackerId, new Position(0, 0));
        
        // Create target
        String targetId = ecsWorld.createEntity();
        ecsWorld.addComponent(targetId, new Stats(100, 100, 5, 10));
        ecsWorld.addComponent(targetId, new Position(10, 10));
        
        // Attack to trigger cooldown
        ecsWorld.addComponent(attackerId, new AttackIntent(targetId));
        combatSystem.tick(0.05f);
        
        // Verify cooldown was set (20 ticks from TestGameConfig)
        var cooldownAfterAttack = ecsWorld.getComponent(attackerId, com.grimoire.server.component.AttackCooldown.class);
        assertTrue(cooldownAfterAttack.isPresent());
        assertEquals(20, cooldownAfterAttack.get().ticksRemaining());
        
        // Tick once more (should decrement cooldown)
        combatSystem.tick(0.05f);
        
        var cooldownAfterOneTick = ecsWorld.getComponent(attackerId, com.grimoire.server.component.AttackCooldown.class);
        assertTrue(cooldownAfterOneTick.isPresent());
        assertEquals(19, cooldownAfterOneTick.get().ticksRemaining());
    }
    
    @Test
    void testAttackCooldownRemovedWhenExpired() {
        // Create attacker with cooldown that's about to expire
        String attackerId = ecsWorld.createEntity();
        ecsWorld.addComponent(attackerId, new Stats(100, 100, 5, 15));
        ecsWorld.addComponent(attackerId, new Position(0, 0));
        ecsWorld.addComponent(attackerId, new com.grimoire.server.component.AttackCooldown(1));
        
        // Tick once (cooldown decrements from 1 to 0 and is removed)
        combatSystem.tick(0.05f);
        
        // Verify cooldown component was removed
        assertFalse(ecsWorld.hasComponent(attackerId, com.grimoire.server.component.AttackCooldown.class));
    }
    
    @Test
    void testCanAttackAfterCooldownExpires() {
        // Create attacker with cooldown that's about to expire
        String attackerId = ecsWorld.createEntity();
        ecsWorld.addComponent(attackerId, new Stats(100, 100, 5, 15));
        ecsWorld.addComponent(attackerId, new Position(0, 0));
        ecsWorld.addComponent(attackerId, new com.grimoire.server.component.AttackCooldown(1));
        
        // Create target
        String targetId = ecsWorld.createEntity();
        ecsWorld.addComponent(targetId, new Stats(100, 100, 5, 10));
        ecsWorld.addComponent(targetId, new Position(10, 10));
        
        // Tick once to expire cooldown
        combatSystem.tick(0.05f);
        
        // Now attack should work
        ecsWorld.addComponent(attackerId, new AttackIntent(targetId));
        combatSystem.tick(0.05f);
        
        // Verify attack dealt damage
        var statsAfterAttack = ecsWorld.getComponent(targetId, Stats.class);
        assertTrue(statsAfterAttack.isPresent());
        assertEquals(90, statsAfterAttack.get().hp()); // 100 - 10
    }
}
