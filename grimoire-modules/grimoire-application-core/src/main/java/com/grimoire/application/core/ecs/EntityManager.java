package com.grimoire.application.core.ecs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages entity lifecycle and ID generation.
 *
 * <p>
 * Entities are simply unique string IDs. This manager tracks which IDs are
 * alive.
 * </p>
 *
 * <p>
 * <strong>Thread-safety:</strong> This class must only be accessed from the
 * single-threaded game loop. Register as a singleton at the assembly layer.
 * </p>
 */
public class EntityManager {

    /** Alive entity IDs. Single-threaded access only — no concurrent map needed. */
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private final Map<String, Boolean> entities = new HashMap<>();

    /**
     * Creates a new entity with a unique ID.
     *
     * @return the entity ID
     */
    public String createEntity() {
        String entityId = UUID.randomUUID().toString();
        entities.put(entityId, Boolean.TRUE);
        return entityId;
    }

    /**
     * Destroys an entity.
     *
     * @param entityId
     *            the entity ID
     */
    public void destroyEntity(String entityId) {
        entities.remove(entityId);
    }

    /**
     * Checks if an entity exists.
     *
     * @param entityId
     *            the entity ID
     * @return true if the entity exists
     */
    public boolean exists(String entityId) {
        return entities.containsKey(entityId);
    }

    /**
     * Gets all entity IDs.
     *
     * @return iterable of all entity IDs
     */
    public Iterable<String> getAllEntityIds() {
        return entities.keySet();
    }
}
