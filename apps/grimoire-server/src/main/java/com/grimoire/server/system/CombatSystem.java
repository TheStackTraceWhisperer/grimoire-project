package com.grimoire.server.system;
import io.micronaut.core.annotation.Order;

import com.grimoire.server.component.AttackCooldown;
import com.grimoire.server.component.AttackIntent;
import com.grimoire.server.component.BoundingBox;
import com.grimoire.server.component.Dead;
import com.grimoire.server.component.Dirty;
import com.grimoire.server.component.Experience;
import com.grimoire.server.component.Monster;
import com.grimoire.server.component.PlayerConnection;
import com.grimoire.server.component.Position;
import com.grimoire.server.component.Stats;
import com.grimoire.server.component.Zone;
import com.grimoire.ecs.EcsWorld;
import com.grimoire.ecs.GameSystem;
import com.grimoire.shared.dto.EntityDespawn;
import com.grimoire.shared.protocol.GamePacket;
import com.grimoire.shared.protocol.PacketType;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles combat logic including attack processing, damage calculation, and death handling.
 * 
 * <p>This system processes {@link AttackIntent} components to apply damage to target entities.
 * When an entity's HP drops to 0 or below, it is marked with a {@link Dead} component.</p>
 * 
 * <p>Combat rules:</p>
 * <ul>
 *   <li>Damage formula: {@code damage = attacker.attack - target.defense}</li>
 *   <li>Minimum damage is always 1 (attacks cannot heal)</li>
 *   <li>Range checking uses {@link Position} and {@link BoundingBox} components</li>
 *   <li>Attack cooldown prevents rapid-fire attacks (configurable via attackCooldownTicks)</li>
 * </ul>
 */
@Order(600)
@Singleton
@Slf4j
public class CombatSystem implements GameSystem {
    
    private final EcsWorld ecsWorld;
    private final SpatialGridSystem spatialGridSystem;
    private final double attackRange;
    private final int attackCooldownTicks;
    
    public CombatSystem(EcsWorld ecsWorld, SpatialGridSystem spatialGridSystem, 
                        com.grimoire.server.config.GameConfig gameConfig) {
        this.ecsWorld = ecsWorld;
        this.spatialGridSystem = spatialGridSystem;
        this.attackRange = gameConfig.getAttackRange();
        this.attackCooldownTicks = gameConfig.getAttackCooldownTicks();
    }
    
    @Override
    public void tick(float deltaTime) {
        processCooldowns();
        processAttacks();
        processDeath();
    }
    
    /**
     * Decrements attack cooldowns and removes expired ones.
     */
    private void processCooldowns() {
        List<String> entitiesWithCooldown = new ArrayList<>();
        for (String entityId : ecsWorld.getEntitiesWithComponent(AttackCooldown.class)) {
            entitiesWithCooldown.add(entityId);
        }
        
        for (String entityId : entitiesWithCooldown) {
            Optional<AttackCooldown> cooldownOpt = ecsWorld.getComponent(entityId, AttackCooldown.class);
            if (cooldownOpt.isPresent()) {
                int remaining = cooldownOpt.get().ticksRemaining() - 1;
                if (remaining <= 0) {
                    ecsWorld.removeComponent(entityId, AttackCooldown.class);
                } else {
                    ecsWorld.addComponent(entityId, new AttackCooldown(remaining));
                }
            }
        }
    }
    
    /**
     * Processes all pending attack intents.
     */
    private void processAttacks() {
        // Collect entities with attack intent (avoid concurrent modification)
        List<String> attackers = new ArrayList<>();
        for (String entityId : ecsWorld.getEntitiesWithComponent(AttackIntent.class)) {
            attackers.add(entityId);
        }
        
        for (String attackerId : attackers) {
            Optional<AttackIntent> intentOpt = ecsWorld.getComponent(attackerId, AttackIntent.class);
            if (intentOpt.isEmpty()) {
                continue;
            }
            
            AttackIntent intent = intentOpt.get();
            String targetId = intent.targetEntityId();
            
            // Remove the attack intent after processing
            ecsWorld.removeComponent(attackerId, AttackIntent.class);
            
            // Check if attacker is on cooldown
            if (ecsWorld.hasComponent(attackerId, AttackCooldown.class)) {
                log.debug("Attack rejected: {} is on cooldown", attackerId);
                continue;
            }
            
            // Validate target exists and is alive
            if (!ecsWorld.entityExists(targetId)) {
                log.debug("Attack target {} does not exist", targetId);
                continue;
            }
            
            if (ecsWorld.hasComponent(targetId, Dead.class)) {
                log.debug("Attack target {} is already dead", targetId);
                continue;
            }
            
            // Check range
            if (!isInRange(attackerId, targetId)) {
                log.debug("Target {} is out of range for attacker {}", targetId, attackerId);
                continue;
            }
            
            // Get attacker and target stats
            Optional<Stats> attackerStatsOpt = ecsWorld.getComponent(attackerId, Stats.class);
            Optional<Stats> targetStatsOpt = ecsWorld.getComponent(targetId, Stats.class);
            
            if (attackerStatsOpt.isEmpty() || targetStatsOpt.isEmpty()) {
                log.debug("Missing stats for combat: attacker={} target={}", 
                         attackerStatsOpt.isPresent(), targetStatsOpt.isPresent());
                continue;
            }
            
            Stats attackerStats = attackerStatsOpt.get();
            Stats targetStats = targetStatsOpt.get();
            
            // Apply attack cooldown
            ecsWorld.addComponent(attackerId, new AttackCooldown(attackCooldownTicks));
            
            // Calculate and apply damage
            int damage = calculateDamage(attackerStats, targetStats);
            int newHp = Math.max(0, targetStats.hp() - damage);
            
            // Create new immutable Stats component with reduced HP
            Stats newTargetStats = new Stats(newHp, targetStats.maxHp(), targetStats.defense(), targetStats.attack());
            ecsWorld.addComponent(targetId, newTargetStats);
            
            // Mark as dirty for network sync
            ecsWorld.addComponent(targetId, new Dirty(ecsWorld.getCurrentTick()));
            
            log.debug("Combat: {} dealt {} damage to {} (HP: {} -> {})", 
                     attackerId, damage, targetId, targetStats.hp(), newHp);
            
            // Mark as dead if HP is zero
            if (newHp <= 0) {
                ecsWorld.addComponent(targetId, new Dead(attackerId));
                log.info("Entity {} was killed by {}", targetId, attackerId);
            }
        }
    }
    
    /**
     * Processes entities marked as dead, sending despawn notifications and awarding XP.
     */
    private void processDeath() {
        // Collect dead entities (avoid concurrent modification)
        List<String> deadEntities = new ArrayList<>();
        for (String entityId : ecsWorld.getEntitiesWithComponent(Dead.class)) {
            deadEntities.add(entityId);
        }
        
        for (String deadEntityId : deadEntities) {
            // Get the zone of the dead entity for broadcasting
            Optional<Zone> zoneOpt = ecsWorld.getComponent(deadEntityId, Zone.class);
            String zone = zoneOpt.map(Zone::zoneId).orElse("unknown");
            
            // Award XP to killer if applicable
            awardXpToKiller(deadEntityId);
            
            // Notify all players in the same zone
            EntityDespawn despawn = new EntityDespawn(deadEntityId);
            GamePacket despawnPacket = new GamePacket(PacketType.S2C_ENTITY_DESPAWN, despawn);
            
            for (String playerId : ecsWorld.getEntitiesWithComponent(PlayerConnection.class)) {
                Optional<Zone> playerZoneOpt = ecsWorld.getComponent(playerId, Zone.class);
                if (playerZoneOpt.isPresent() && playerZoneOpt.get().zoneId().equals(zone)) {
                    ecsWorld.getComponent(playerId, PlayerConnection.class).ifPresent(conn -> 
                        conn.channel().writeAndFlush(despawnPacket));
                }
            }
            
            // Remove from spatial grid before destroying (keeps grid in sync)
            spatialGridSystem.removeEntity(deadEntityId);
            
            // Destroy the entity
            ecsWorld.destroyEntity(deadEntityId);
            log.debug("Destroyed dead entity {}", deadEntityId);
        }
    }
    
    /**
     * Awards XP to the killer if the dead entity was a monster.
     * 
     * @param deadEntityId the entity that died
     */
    private void awardXpToKiller(String deadEntityId) {
        Optional<Dead> deadOpt = ecsWorld.getComponent(deadEntityId, Dead.class);
        if (deadOpt.isEmpty() || deadOpt.get().killerId() == null) {
            return;
        }
        
        String killerId = deadOpt.get().killerId();
        
        // Check if dead entity was a monster (provides XP)
        Optional<Monster> monsterOpt = ecsWorld.getComponent(deadEntityId, Monster.class);
        if (monsterOpt.isEmpty()) {
            return;
        }
        
        int xpReward = monsterOpt.get().xpReward();
        
        // Check if killer has Experience component (players do)
        if (!ecsWorld.entityExists(killerId) || !ecsWorld.hasComponent(killerId, Experience.class)) {
            return;
        }
        
        Experience currentExp = ecsWorld.getComponent(killerId, Experience.class).orElseThrow();
        int newXp = currentExp.currentXp() + xpReward;
        
        ecsWorld.addComponent(killerId, new Experience(newXp, currentExp.xpToNextLevel()));
        ecsWorld.addComponent(killerId, new Dirty(ecsWorld.getCurrentTick()));
        
        log.info("Entity {} gained {} XP from killing {} (total: {})", 
                killerId, xpReward, deadEntityId, newXp);
    }
    
    /**
     * Calculates damage based on attacker and target stats.
     * 
     * @param attacker the attacker's stats
     * @param target the target's stats
     * @return the damage amount (minimum 1)
     */
    private int calculateDamage(Stats attacker, Stats target) {
        int rawDamage = attacker.attack() - target.defense();
        return Math.max(1, rawDamage);
    }
    
    /**
     * Checks if the attacker is within range of the target.
     * 
     * @param attackerId the attacker entity ID
     * @param targetId the target entity ID
     * @return true if in range, false otherwise
     */
    private boolean isInRange(String attackerId, String targetId) {
        Optional<Position> attackerPosOpt = ecsWorld.getComponent(attackerId, Position.class);
        Optional<Position> targetPosOpt = ecsWorld.getComponent(targetId, Position.class);
        
        if (attackerPosOpt.isEmpty() || targetPosOpt.isEmpty()) {
            return false;
        }
        
        Position attackerPos = attackerPosOpt.get();
        Position targetPos = targetPosOpt.get();
        
        double dx = attackerPos.x() - targetPos.x();
        double dy = attackerPos.y() - targetPos.y();
        double distanceSquared = dx * dx + dy * dy;
        
        return distanceSquared <= attackRange * attackRange;
    }
}
