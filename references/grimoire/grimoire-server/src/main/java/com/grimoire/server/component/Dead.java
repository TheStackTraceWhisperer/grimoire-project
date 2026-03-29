package com.grimoire.server.component;

/**
 * Dead component marking an entity for death/despawn processing.
 * 
 * <p>Entities with this component should be despawned on the next sync
 * and then removed from the ECS world.</p>
 * 
 * @param killerId the entity ID that killed this entity (null for environmental deaths)
 */
public record Dead(String killerId) implements Component {
}
