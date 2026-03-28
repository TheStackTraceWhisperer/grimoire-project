package com.grimoire.ecs;

import jakarta.inject.Singleton;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages entity lifecycle and ID generation.
 */
@Singleton
@SuppressWarnings("PMD.CommentRequired")
public class EntityManager {
    
    private final Map<String, Boolean> entities = new ConcurrentHashMap<>();
    
    /**
     * Creates a new entity with a unique ID.
     * @return the entity ID
     */
    public String createEntity() {
        String entityId = UUID.randomUUID().toString();
        entities.put(entityId, true);
        return entityId;
    }
    
    /**
     * Destroys an entity.
     * @param entityId the entity ID
     */
    public void destroyEntity(String entityId) {
        entities.remove(entityId);
    }
    
    /**
     * Checks if an entity exists.
     * @param entityId the entity ID
     * @return true if the entity exists
     */
    public boolean exists(String entityId) {
        return entities.containsKey(entityId);
    }
    
    /**
     * Gets all entity IDs.
     * @return collection of all entity IDs
     */
    public Iterable<String> getAllEntityIds() {
        return entities.keySet();
    }
}
