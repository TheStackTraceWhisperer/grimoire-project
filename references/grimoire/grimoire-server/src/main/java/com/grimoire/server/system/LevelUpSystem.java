package com.grimoire.server.system;

import com.grimoire.server.component.Dirty;
import com.grimoire.server.component.Experience;
import com.grimoire.server.component.Persistent;
import com.grimoire.server.component.Stats;
import com.grimoire.server.ecs.EcsWorld;
import com.grimoire.server.ecs.GameSystem;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles level up progression for player entities.
 * 
 * <p>This system monitors the {@link Experience} component and triggers level ups
 * when {@code currentXp >= xpToNextLevel}. Upon leveling up:</p>
 * <ul>
 *   <li>Level is incremented (tracked via Persistent + database)</li>
 *   <li>Stats are boosted (maxHp, attack, defense)</li>
 *   <li>XP threshold for next level increases</li>
 *   <li>Current XP rolls over any excess</li>
 * </ul>
 */
@Singleton
@RequiredArgsConstructor
@Slf4j
public class LevelUpSystem implements GameSystem {
    
    private static final int HP_PER_LEVEL = 10;
    private static final int ATTACK_PER_LEVEL = 2;
    private static final int DEFENSE_PER_LEVEL = 1;
    private static final double XP_SCALING_FACTOR = 1.5;
    
    private final EcsWorld ecsWorld;
    
    @Override
    public void tick(float deltaTime) {
        processLevelUps();
    }
    
    /**
     * Checks all entities with Experience component for level up eligibility.
     */
    private void processLevelUps() {
        List<String> entitiesWithExp = new ArrayList<>();
        for (String entityId : ecsWorld.getEntitiesWithComponent(Experience.class)) {
            entitiesWithExp.add(entityId);
        }
        
        for (String entityId : entitiesWithExp) {
            Optional<Experience> expOpt = ecsWorld.getComponent(entityId, Experience.class);
            if (expOpt.isEmpty()) {
                continue;
            }
            
            Experience exp = expOpt.get();
            
            // Check if eligible for level up
            while (exp.currentXp() >= exp.xpToNextLevel()) {
                exp = levelUp(entityId, exp);
            }
        }
    }
    
    /**
     * Performs a level up for the entity.
     * 
     * @param entityId the entity leveling up
     * @param currentExp the current experience component
     * @return the new experience component after leveling
     */
    private Experience levelUp(String entityId, Experience currentExp) {
        // Calculate remaining XP after level up
        int remainingXp = currentExp.currentXp() - currentExp.xpToNextLevel();
        
        // Calculate new XP threshold (scales with each level)
        int newXpToNextLevel = (int) (currentExp.xpToNextLevel() * XP_SCALING_FACTOR);
        
        // Create new experience component
        Experience newExp = new Experience(remainingXp, newXpToNextLevel);
        ecsWorld.addComponent(entityId, newExp);
        
        // Boost stats
        Optional<Stats> statsOpt = ecsWorld.getComponent(entityId, Stats.class);
        if (statsOpt.isPresent()) {
            Stats oldStats = statsOpt.get();
            int newMaxHp = oldStats.maxHp() + HP_PER_LEVEL;
            int newHp = Math.min(oldStats.hp() + HP_PER_LEVEL, newMaxHp); // Heal the HP gained from level up, but do not exceed new max
            int newAttack = oldStats.attack() + ATTACK_PER_LEVEL;
            int newDefense = oldStats.defense() + DEFENSE_PER_LEVEL;
            
            Stats newStats = new Stats(newHp, newMaxHp, newDefense, newAttack);
            ecsWorld.addComponent(entityId, newStats);
        }
        
        // Mark as dirty for network sync
        ecsWorld.addComponent(entityId, new Dirty(ecsWorld.getCurrentTick()));
        
        // Get entity info for logging
        String entityInfo = ecsWorld.getComponent(entityId, Persistent.class)
                .map(p -> "Character " + p.accountId())
                .orElse("Entity " + entityId);
        
        log.info("{} leveled up! New XP threshold: {}", entityInfo, newXpToNextLevel);
        
        return newExp;
    }
}
