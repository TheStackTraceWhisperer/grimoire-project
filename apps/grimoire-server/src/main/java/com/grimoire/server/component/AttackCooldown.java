package com.grimoire.server.component;
import com.grimoire.ecs.Component;

/**
 * Component tracking attack cooldown for an entity.
 * 
 * <p>Prevents rapid-fire attacks by enforcing a cooldown period between attacks.
 * This is a security measure to prevent clients from spamming attack packets
 * faster than the intended attack speed.</p>
 * 
 * @param ticksRemaining the number of ticks until the entity can attack again
 */
public record AttackCooldown(int ticksRemaining) implements Component {
}
